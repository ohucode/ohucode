(ns ohucode.handler
  (:use [misaeng.core]
        [compojure.core]
        [ring.util.response]
        [ohucode.core])
  (:require [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [ohucode.db :as db]
            [ohucode.view-top :as v-top]
            [ohucode.handler-git :refer [smart-http-routes]]
            [ohucode.handler-admin :refer [admin-routes]]
            [ohucode.handler-signup :refer [signup-routes]]
            [ohucode.handler-templates :refer [template-routes]]))

(함수 wrap-signed-user-only [handler]
  (fn [req]
    (만약-가정 [signed-in? req]
      (handler req)
      (v-top/request-error "로그인이 필요합니다."))))

(함수- 로그인 [res userid]
  (assoc-in res [:session :user]
            (-> (db/select-user userid)
                (dissoc :password_digest :created_at :updated_at))))

(정의 user-routes
  (routes
   (context "/user" []
     (GET "/login" req v-top/login-page)
     (POST "/login" [userid password]
       (if (db/valid-user-password? userid password)
         (-> (redirect "/")
             (assoc :flash "로그인 성공")
             (로그인 userid))
         (-> (redirect "/user/login")
             (assoc :flash "인증 실패"))))
     (GET "/logout" req
       (db/insert-audit (or (:userid (session-user req))
                            "guest")
                        "logout" {})
       (-> (v-top/basic-content req "로그아웃" "로그아웃처리")
           response
           (update :session dissoc :user))))
   (context "/:user" [user]
     (GET "/" [] v-top/not-found)
     (GET "/settings" [] v-top/not-implemented)
     (GET "/profile" [] v-top/not-implemented))))

(정의 project-routes
  (context "/:user/:project" [user project]
    (GET "/" [] v-top/not-found)
    (GET "/commits" [] v-top/not-implemented)
    (GET "/commits/:ref" [ref] v-top/not-implemented)
    (GET "/commit/:commit-id" [commit-id] v-top/not-implemented)
    (GET "/settings" [] v-top/not-implemented)
    (GET "/tree/:ref/:path" [ref path] v-top/not-implemented)
    (GET "/blob/:ref/:path" [ref path] v-top/not-implemented)
    (GET "/tags" [] v-top/not-implemented)
    (GET "/branches" [] v-top/not-implemented)
    (GET "/issues" [] v-top/not-implemented)))

(정의 web-routes
  (routes
   (GET "/" req
     (만약 (signed-in? req)
       v-top/dashboard
       v-top/intro-guest))
   (GET "/throw" [] (throw (RuntimeException. "스택트레이스 실험")))
   (GET "/terms-of-service" [] v-top/terms-of-service)
   (GET "/privacy-policy" [] v-top/privacy-policy)
   (GET "/credits" [] v-top/credits)
   signup-routes
   admin-routes
   user-routes
   project-routes))

(함수- wrap-html-content-type [handler]
  (fn [req]
    (만약-가정 [res (handler req)]
      (만약 (find-header res "Content-Type")
        res
        (content-type res "text/html; charset=utf-8")))))

(함수 wrap-bind-client-ip [handler]
  (fn [req]
    (binding [*client-ip* (:remote-addr req)]
      (handler req))))

(defonce ^:private
  ^{:doc "리로드 해도 세션을 유지하기 위해 메모리 세션 따로 둡니다"}
  세션저장소 (memory-store))

(정의 app
  (routes
   (route/resources "/js" {:root "public/js"})
   (route/resources "/css" {:root "public/css"})

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

   (ANY "*" [] v-top/not-found)))

(정의 app-dev
  (-> (routes template-routes app)
      (wrap-exceptions)
      (wrap-reload)))
