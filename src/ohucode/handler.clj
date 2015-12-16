(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [ohucode.auth :as a]
            [ohucode.view-top :as v-top]
            [ohucode.handler-git :refer [smart-http-routes]]
            [ohucode.handler-admin :refer [admin-routes]]
            [ohucode.handler-signup :refer [signup-routes]]
            [ohucode.handler-templates :refer [template-routes]]))

(defn- not-implemented [req]
  (throw (UnsupportedOperationException.)))

(def user-routes
  (context "/:user" [user]
    (GET "/" [] v-top/not-found)
    (GET "/settings" [] not-implemented)
    (GET "/profile" [] not-implemented)))

(def project-routes
  (context "/:user/:project" [user project]
    (GET "/" [] v-top/not-found)
    (GET "/commits" [] not-implemented)
    (GET "/commits/:ref" [ref] not-implemented)
    (GET "/commit/:commit-id" not-implemented-yet)
    (GET "/settings" [] not-implemented)
    (GET "/tree/:ref/:path" [ref path] not-implemented)
    (GET "/blob/:ref/:path" [ref path] not-implemented)
    (GET "/tags" [] not-implemented)
    (GET "/branches" [] not-implemented)
    (GET "/issues" [] not-implemented)))

(def web-routes
  (routes
   (GET "/" req
     (if (a/auth? req)
       (v-top/intro-guest req) (v-top/intro-guest req)))
   (GET "/logout" [] "로그아웃처리")
   (GET "/throw" [] (throw (RuntimeException. "스택트레이스 실험")))
   (GET "/terms-of-service" [] v-top/terms-of-service)
   (GET "/privacy-policy" [] v-top/privacy-policy)
   signup-routes
   admin-routes
   user-routes
   project-routes))

(def app
  (routes
   (route/resources "/js" {:root "public/js"})
   (route/resources "/css" {:root "public/css"})

   ;; compojure.core/wrap-routes는 라우트 매칭시에만 미들웨어를 입힌다.
   ;; -> wrap-params 미들웨어가 핸들러 매치 여부와 무관하게,
   ;;    form-encoded 본문을 먼저 읽는 문제를 해결.
   (wrap-routes smart-http-routes
                wrap-defaults api-defaults)

   (wrap-defaults web-routes
                  (dissoc site-defaults :static))
   (ANY "*" [] v-top/not-found)))

(def app-dev
  (-> (routes template-routes app)
      (wrap-exceptions)
      (wrap-reload)))
