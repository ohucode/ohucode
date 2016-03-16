(ns 오후코드.핸들러
  (:use [미생.기본]
        [오후코드.기본]
        [compojure.core]
        [ring.util.response])
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [clojure.edn :as edn]
            [오후코드.db :as db]
            [오후코드.보안 :as 보안]
            [오후코드.뷰 :as 뷰]
            [오후코드.핸들러-깃 :refer [smart-http-routes]]
            [오후코드.핸들러-관리 :refer [관리-라우트]]
            [오후코드.핸들러-가입 :refer [가입-라우트]]))

(함수 wrap-signed-user-only
  "로그인한 세션만 핸들러를 처리하고, 그렇지 않으면 에러페이지를 보낸다."
  [핸들러]
  (fn [요청]
    (만약 (로그인? 요청)
      (핸들러 요청)
      (뷰/요청에러 "로그인이 필요합니다."))))

(함수- 로그인 [응답 아이디]
  (assoc-in 응답 [:session :user]
            (-> (db/select-user 아이디)
                (dissoc :password_digest :created_at :updated_at))))

(함수- 지금시각 []
  (quot (System/currentTimeMillis) 1000))

(정의 인증쿠키명 "ohucode-auth")

(가정 [유통기한 (* 7 24 3600)]
  (정의 ^:private 인증쿠키-기본값
    {:value ""
     :path "/"
     :max-age 유통기한
     :secure false ;; HTTPS 연결후 true로 바꾸자
     :http-only true})

  (함수- 인증쿠키 [아이디]
    (assoc 인증쿠키-기본값
           :value (보안/인증토큰생성
                   {:아이디   아이디
                    :발급일시 (지금시각)
                    :만료일시 (+ (지금시각) 유통기한)}))))

(함수- 인증쿠키삭제 [응답]
  (assoc-in 응답 [:cookies 인증쿠키명]
            (assoc 인증쿠키-기본값 :max-age 0)))

(함수- 로그인-미들웨어 [핸들러 아이디]
  (fn [요청]
    (가정 [이용자 (db/select-user 아이디)
           요청' (-> 요청
                     (assoc-in [:session :이용자] 이용자)
                     (assoc-in [:오후코드 :로그인처리] true))
           응답 (핸들러 요청')]
      (cond-> 응답       ; TODO: 최종 로그인 시간 기록
        true
        (assoc-in [:session :이용자] 이용자)

        (= :없음 (get-in 응답 [:cookies 인증쿠키명] :없음))
        (assoc-in [:cookies 인증쿠키명] (인증쿠키 아이디))))))

(함수- 로그아웃응답 [응답]
  (-> 응답
      (assoc-in [:session :이용자] nil) ; TODO: nil로 삭제되는건지 확인 필요
      인증쿠키삭제))

(정의 이용자-라우트
  (routes
   (context "/user" []
     (POST "/login" [아이디 비밀번호 :as 요청]
       (만약 (db/valid-user-password? 아이디 비밀번호)
         (가정 [이용자 (db/select-user 아이디) ; TODO: 중복 제거
                핸들러 (fn [_] {:status 200 :body {:이용자 이용자}})
                로그인처리 (로그인-미들웨어 핸들러 아이디)]
           (로그인처리 요청))
         {:status 401 :body {:실패 "인증 실패"}}))
     (PUT "/logout" 요청
       (db/insert-audit (get (세션이용자 요청) :아이디 "손님")
                        "로그아웃" {})
       (로그아웃응답 {:status 200
                      :body {:성공 "로그아웃 처리"}})))
   (context "/:아이디" [아이디]
     (GET "/" [] (if-let [이용자 (db/select-user 아이디)]
                   (str 이용자)))
     (POST "/" [] "TODO: 프로젝트생성")
     (GET "/settings" [] 뷰/미구현)
     (GET "/profile" [] 뷰/미구현))))

(정의 프로젝트-라우트
  (context "/:아이디/:프로젝트" [아이디 프로젝트]
    (GET "/" [] 뷰/not-found)
    (GET "/commits" [] 뷰/미구현)
    (GET "/commits/:ref" [ref] 뷰/미구현)
    (GET "/commit/:commit-id" [commit-id] 뷰/미구현)
    (GET "/settings" [] 뷰/미구현)
    (GET "/tree/:ref/:path" [ref path] 뷰/미구현)
    (GET "/blob/:ref/:path" [ref path] 뷰/미구현)
    (GET "/tags" [] 뷰/미구현)
    (GET "/branches" [] 뷰/미구현)
    (GET "/issues" [] 뷰/미구현)))

(정의 웹-라우트
  (routes
   (GET "/" [] 뷰/기본)
   (GET "/throw" [] (예외발생 (RuntimeException. "스택트레이스 실험")))
   (GET "/tos" [] 뷰/기본)
   (GET "/policy" [] 뷰/기본)
   (GET "/credits" [] 뷰/기본)
   가입-라우트
   관리-라우트
   이용자-라우트
   프로젝트-라우트))

(함수- wrap-콘텐트타입 [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (find-header 응답 "Content-Type")
        응답
        (content-type 응답 "text/html; charset=utf-8")))))

(함수- wrap-client-ip-표시 [핸들러]
  (fn [요청]
    (바인딩 [*client-ip* (:remote-addr 요청)]
      (핸들러 요청))))

(함수- 손님전용-미들웨어
  "로그인하지 않은 세션의 경우에만 미들웨어를 끼운다.
   로그인한 세션은 미들웨어 거치지 않고 바로 핸들러 처리한다."
  [핸들러 미들웨어]
  (가정 [미들웨어-낀-핸들러 (미들웨어 핸들러)]
    (fn [요청]
      ((if (로그인? 요청) 핸들러 미들웨어-낀-핸들러) 요청))))

(함수- 인증쿠키삭제-미들웨어
  [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (get-in 응답 [:cookies 인증쿠키명])
        응답 ; 로그인 처리일 경우, 응답에서 인증 쿠키가 새로 생긴다. 이때는 유지.
        (인증쿠키삭제 응답)))))

(함수- wrap-인증쿠키확인
  "인증쿠키를 확인해서 유효하면, 요청에 :인증 정보를 포함시키고,
  무효하면, 인증쿠키를 삭제한다. 이미 인증된 세션의 경우 아무런
  처리도 하지 않는다.
  중요: 쿠키/세션 미들웨어보다 안쪽에 등록해야한다."
  [핸들러]
  (가정 [인증쿠키삭제-핸들러 (인증쿠키삭제-미들웨어 핸들러)]
    (fn [요청]
      (가정 [인증정보 (-> 요청
                          (get-in [:cookies 인증쿠키명 :value])
                          보안/인증토큰확인)]
        (만약 (and 인증정보
                   (< (지금시각) (get 인증정보 :만료일시 0)))
          ((로그인-미들웨어 핸들러 (:아이디 인증정보)) 요청)
          (인증쿠키삭제-핸들러 요청))))))

(함수- wrap-edn-파라미터
  "요청 컨텐트 타입이 application/edn이면 요청 본문을 EDN 형태로 읽어서 :params 맵에 추가한다."
  [핸들러]
  (가정 [edn? (fn [요청]
                (and
                 (re-find #"^application/edn" (get-in 요청 [:headers "content-type"] ""))
                 (:body 요청)))
         ;; [주의] clojure.core/read-string은 eval이 되므로 쓰지 않는다.
         ;; http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/read
         ;; clojure.edn/read-string은 괜찮습니다.
         read-edn (합성 edn/read-string slurp)]
    (fn [요청]
      (만약-가정 [본문 (edn? 요청)]
        (핸들러 (assoc 요청 :params (병합 (:params 요청) (read-edn 본문))))
        (핸들러 요청)))))

(함수- wrap-edn-응답
  "응답 본문이 맵이면 EDN 표기로 바꿔 보낸다."
  [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (map? (:body 응답))
        (-> 응답
            (assoc :body (pr-str (:body 응답)))
            (content-type "application/edn; charset=utf-8"))
        응답))))

(함수- wrap-이용자정보 [핸들러]
  (fn [요청]
    (바인딩 [*세션이용자* (세션이용자 요청)]
      (핸들러 요청))))

(한번정의
 ^{:private true
   :doc "리로드해도 세션을 유지하기 위해 메모리 세션 따로 둔다"}
  세션저장소 (memory-store))

(정의 app
  (routes
   (route/resources "/js" {:root "public/js"})
   (route/resources "/css" {:root "public/css"})
   (route/resources "/md" {:root "public/md"})

   (wrap-routes smart-http-routes
                wrap-defaults api-defaults)

   (-> 웹-라우트
       wrap-with-logger     ; TODO: 로거 위치 고민 필요
       wrap-이용자정보
       wrap-client-ip-표시
       wrap-콘텐트타입
       (손님전용-미들웨어 wrap-인증쿠키확인)
       wrap-edn-응답
       wrap-edn-파라미터
       (wrap-defaults (-> site-defaults
                          ;; static 자원은 앞에서 미리 처리한다
                          (dissoc :static)
                          (dissoc :security) ;; TODO: AJAX CSRF 대응
                          (assoc-in [:session :store] 세션저장소))))

   (ANY "*" [] 뷰/not-found)))

(정의 app-dev
  (-> app
      wrap-exceptions
      wrap-reload))
