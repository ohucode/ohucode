(ns 오후코드.핸들러
  (:use [미생.기본]
        [오후코드.기본]
        [compojure.core]
        [ring.util.response])
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [오후코드.db :as db]
            [오후코드.뷰-최상 :as 최상뷰]
            [오후코드.핸들러-깃 :refer [smart-http-routes]]
            [오후코드.핸들러-관리 :refer [관리-라우트]]
            [오후코드.핸들러-가입 :refer [가입-라우트]]
            [오후코드.핸들러-템플릿 :refer [템플릿-라우트]]))

(함수 wrap-signed-user-only [핸들러]
  (fn [요청]
    (만약-가정 [로그인? 요청]
      (핸들러 요청)
      (최상뷰/요청에러 "로그인이 필요합니다."))))

(함수- 로그인 [응답 아이디]
  (assoc-in 응답 [:session :user]
            (-> (db/select-user 아이디)
                (dissoc :password_digest :created_at :updated_at))))

(정의 user-routes
  (routes
   (context "/user" []
     (GET "/login" 요청 최상뷰/로그인-페이지)
     (POST "/login" [userid password]
       (만약 (db/valid-user-password? userid password)
         (-> (redirect "/")
             (assoc :flash "로그인 성공")
             (로그인 userid))
         (-> (redirect "/user/login")
             (assoc :flash "인증 실패"))))
     (GET "/logout" 요청
       (db/insert-audit (or (:userid (session-user 요청))
                            "guest")
                        "logout" {})
       (-> (최상뷰/basic-content 요청 "로그아웃" "로그아웃처리")
           response
           (update :session dissoc :user))))
   (context "/:user" [user]
     (GET "/" [] 최상뷰/not-found)
     (GET "/settings" [] 최상뷰/미구현)
     (GET "/profile" [] 최상뷰/미구현))))

(정의 project-routes
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

(정의 web-routes
  (routes
   (GET "/" 요청
     (만약 (로그인? 요청)
       최상뷰/대시보드
       최상뷰/intro-guest))
   (GET "/throw" [] (예외발생 (RuntimeException. "스택트레이스 실험")))
   (GET "/terms-of-service" [] 최상뷰/terms-of-service)
   (GET "/privacy-policy" [] 최상뷰/privacy-policy)
   (GET "/credits" [] 최상뷰/credits)
   가입-라우트
   관리-라우트
   user-routes
   project-routes))

(함수- wrap-html-content-type [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (find-header 응답 "Content-Type")
        응답
        (content-type 응답 "text/html; charset=utf-8")))))

(함수 wrap-bind-client-ip [핸들러]
  (fn [요청]
    (binding [*client-ip* (:remote-addr 요청)]
      (핸들러 요청))))

(한번정의 ^:private
  ^{:doc "리로드해도 세션을 유지하기 위해 메모리 세션 따로 둡니다"}
  세션저장소 (memory-store))

(정의 app
  (routes
   (route/resources "/js" {:root "public/js"})
   (route/resources "/cljs" {:root "public/cljs"})
   (route/resources "/cljs.min" {:root "public/cljs.min"})
   (route/resources "/css" {:root "public/css"})
   (route/resources "/md" {:root "public/md"})

   (wrap-routes smart-http-routes
                wrap-defaults api-defaults)

   (-> web-routes
       wrap-user-info
       wrap-bind-client-ip
       wrap-html-content-type
       (wrap-defaults (-> site-defaults
                          ;; static 자원은 앞에서 미리 처리합니다
                          (dissoc :static)
                          (assoc-in [:session :store] 세션저장소))))

   (ANY "*" [] 최상뷰/not-found)))

(정의 app-dev
  (-> (routes 템플릿-라우트 app)
      (wrap-exceptions)
      (wrap-reload)))
