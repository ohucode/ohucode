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
            [clojure.edn]
            [오후코드.db :as db]
            [오후코드.뷰 :as 뷰]
            [오후코드.뷰-최상 :as 최상뷰]
            [오후코드.핸들러-깃 :refer [smart-http-routes]]
            [오후코드.핸들러-관리 :refer [관리-라우트]]
            [오후코드.핸들러-가입 :refer [가입-라우트]]
            [오후코드.핸들러-템플릿 :refer [템플릿-라우트]]))

(함수 wrap-signed-user-only [핸들러]
  (fn [요청]
    (만약 (로그인? 요청)
      (핸들러 요청)
      (최상뷰/요청에러 "로그인이 필요합니다."))))

(함수- 로그인 [응답 아이디]
  (assoc-in 응답 [:session :user]
            (-> (db/select-user 아이디)
                (dissoc :password_digest :created_at :updated_at))))

(정의 이용자-라우트
  (routes
   (context "/user" []
     (POST "/login" [아이디 비밀번호]
       (만약 (db/valid-user-password? 아이디 비밀번호)
         {:status 200 :body {:message "로그인 성공"}} ;; TODO: 로그인 쿠키 or 세션 설정합시다.
         {:status 401 :body {:message "인증 실패"}}))
     (GET "/logout" 요청
       (db/insert-audit (or (:userid (session-user 요청))
                            "guest")
                        "logout" {})
       ;; TODO: 로그인 쿠기 or 세션 제거
       {:status 200 :body {:message "로그아웃 처리"}}))
   (context "/:user" [user]
     (GET "/" [] 최상뷰/not-found)
     (GET "/settings" [] 최상뷰/미구현)
     (GET "/profile" [] 최상뷰/미구현))))

(정의 프로젝트-라우트
  (context "/:user/:project" [user project]
    (GET "/" [] 최상뷰/not-found)
    (GET "/commits" [] 최상뷰/미구현)
    (GET "/commits/:ref" [ref] 최상뷰/미구현)
    (GET "/commit/:commit-id" [commit-id] 최상뷰/미구현)
    (GET "/settings" [] 최상뷰/미구현)
    (GET "/tree/:ref/:path" [ref path] 최상뷰/미구현)
    (GET "/blob/:ref/:path" [ref path] 최상뷰/미구현)
    (GET "/tags" [] 최상뷰/미구현)
    (GET "/branches" [] 최상뷰/미구현)
    (GET "/issues" [] 최상뷰/미구현)))

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

(함수- wrap-html-content-type [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (find-header 응답 "Content-Type")
        응답
        (content-type 응답 "text/html; charset=utf-8")))))

(함수- wrap-bind-client-ip [핸들러]
  (fn [요청]
    (binding [*client-ip* (:remote-addr 요청)]
      (핸들러 요청))))

(함수- wrap-edn-params
  "요청 컨텐트 타입이 application/edn이면 요청 본문을 EDN 형태로 읽어서 :params 맵에 추가합니다."
  [핸들러]
  (가정 [edn? (fn [요청]
                (and
                 (= "application/edn" (get-in 요청 [:headers "content-type"]))
                 (:body 요청)))

         ;; [주의] clojure.core/read-string은 eval이 되므로 쓰지 않습니다.
         ;; http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/read
         ;; clojure.edn/read-string은 괜찮습니다.
         read-edn (합성 clojure.edn/read-string slurp)]
    (fn [요청]
      (만약-가정 [본문 (edn? 요청)]
        (핸들러 (assoc 요청 :params (병합 (:params 요청) (read-edn 본문))))
        (핸들러 요청)))))

(함수- wrap-edn-response
  "응답 본문이 맵이면 EDN 표기로 바꿔 보냅니다."
  [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (map? (:body 응답))
        (-> 응답
            (assoc :body (pr-str (:body 응답)))
            (content-type "application/edn"))
        응답))))

(한번정의
 ^{:private true
   :doc "리로드해도 세션을 유지하기 위해 메모리 세션 따로 둡니다"}
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
       wrap-user-info
       wrap-bind-client-ip
       wrap-html-content-type
       wrap-edn-response
       wrap-edn-params
       (wrap-defaults (-> site-defaults
                          ;; static 자원은 앞에서 미리 처리합니다
                          (dissoc :static)
                          (dissoc :security) ;; TODO: AJAX CSRF 대응합시다.
                          (assoc-in [:session :store] 세션저장소))))

   (ANY "*" [] 최상뷰/not-found)))

(정의 app-dev
  (-> (routes 템플릿-라우트 app)
      wrap-exceptions
      wrap-reload))
