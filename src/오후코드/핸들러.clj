(ns 오후코드.핸들러
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [api-defaults
                                              site-defaults
                                              wrap-defaults]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.util.response :refer :all]
            [미생.기본 :refer :all]
            [오후코드.기본 :refer :all]
            [오후코드.뷰 :as 뷰]
            [오후코드.핸들러-가입 :refer [가입-라우트]]
            [오후코드.핸들러-관리 :refer [관리-라우트]]
            [오후코드.핸들러-깃 :refer [smart-http-라우트]]
            [오후코드.핸들러-로그인 :refer [로그인-라우트 자동로그인-미들웨어]]
            [오후코드.핸들러-유틸 :refer :all]
            [오후코드.핸들러-이름공간 :refer [이름공간-라우트]]
            [오후코드.핸들러-프로젝트 :refer [프로젝트-라우트]]))

;; TODO 기본 URL은 뷰 템플릿 깔아주고,
;; AJAX요청일 때만, EDN/JSON 응답하게 나눠서 처리해야한다.

(defroutes 웹-라우트
  (GET "/" [] 뷰/기본)
  (GET "/throw" [] (예외발생 (RuntimeException. "스택트레이스 실험")))
  (GET "/tos" [] 뷰/기본)
  (GET "/policy" [] 뷰/기본)
  (GET "/credits" [] 뷰/기본)
  가입-라우트
  관리-라우트
  로그인-라우트
  이름공간-라우트
  프로젝트-라우트)

(함수- 콘텐트타입-미들웨어 [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (find-header 응답 "Content-Type")
        응답
        (content-type 응답 "text/html; charset=utf-8")))))

(함수- ip-바인딩-미들웨어 [핸들러]
  (fn [요청]
    (바인딩 [*클라이언트IP* (:remote-addr 요청)]
      (핸들러 요청))))

(함수 이용자-바인딩-미들웨어 [핸들러]
  (fn [요청]
    (바인딩 [*세션이용자* (세션이용자 요청)]
      (핸들러 요청))))

(함수- edn-파라미터-미들웨어
  "요청 컨텐트 타입이 application/edn이면 요청 본문을 EDN 형태로 읽어서 :params 맵에 추가한다."
  [핸들러]
  (가정 [edn읽기 (합성 edn/read-string slurp)
         ;; [주의] clojure.core/read-string은 eval이 되므로 쓰지 않는다.
         ;; http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/read
         ;; clojure.edn/read-string은 괜찮습니다.
         edn병합 (fn [요청]
                   (만약-가정 [본문 (:body 요청)]
                     (update 요청 :params 병합 (edn읽기 본문))
                     요청))]
    (fn [요청]
      (핸들러 (조건-> 요청
                (edn요청? 요청) edn병합)))))

(함수- edn-응답-미들웨어
  "응답 본문이 맵이면 EDN 표기로 바꿔 보낸다."
  [핸들러]
  (fn [요청]
    (만약-가정 [{본문 :body :as 응답} (핸들러 요청)]
      (만약 (map? 본문)
        (-> 응답
            (assoc :body (pr-str 본문))
            (content-type "application/edn; charset=utf-8"))
        응답))))

(정의 ^:private edn-미들웨어
  (합성 edn-파라미터-미들웨어 edn-응답-미들웨어))

(한번정의
 ^{:private true
   :doc "리로드해도 세션을 유지하기 위해 메모리 세션 따로 둔다"}
  세션저장소 (memory-store))

(defn- wrap-logger [handler]
  (fn [req]
    (let [res (handler req)]
      (-> {:요청 (merge (select-keys req [:request-method :path :params])
                        #_{:accept (get-in req [:headers "accept"])}
                        {:path (ring.util.codec/url-decode (:uri req))})
           :응답 (select-keys res [:status :headers :body])}
          pp/pprint
          with-out-str
          로그)
      res)))

(defroutes 앱라우트
  (route/resources "/js"  {:root "public/js"})
  (route/resources "/css" {:root "public/css"})
  (route/resources "/md"  {:root "public/md"})

  (wrap-routes smart-http-라우트
               wrap-defaults api-defaults)

  (-> 웹-라우트
      wrap-logger          ; TODO: 로거 위치 고민 필요
      이용자-바인딩-미들웨어
      ip-바인딩-미들웨어
      콘텐트타입-미들웨어
      자동로그인-미들웨어
      edn-미들웨어
      (wrap-defaults (-> site-defaults
                         ;; static 자원은 앞에서 미리 처리한다
                         (dissoc :static)
                         (dissoc :security) ;; TODO: AJAX CSRF 대응
                         (assoc-in [:session :store] 세션저장소))))

  (rfn _ 뷰/not-found))

(정의 앱-dev
  (-> 앱라우트)) ;; 현재 개발모드 특화 미들웨어 없음
