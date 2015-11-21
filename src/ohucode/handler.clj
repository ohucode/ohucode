(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.middleware.anti-forgery :as af]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [ohucode.auth :as a]
            [ohucode.view :as v]
            [ohucode.git :as git]
            [ohucode.git-http :refer [smart-http-routes]]
            [ohucode.db :as db]
            [ohucode.admin-handler :as admin]))

(defn- not-implemented [req]
  (throw (UnsupportedOperationException.)))

(def restricted-usernames
  ["admin" "js" "css" "fonts" "sign-up" "login" "logout" "fonts"
   "settings" "help" "support" "notifications" "notification"
   "status" "components" "news" "account" "templates"
   "terms-of-service" "privacy-policy"])

(def user-routes
  (context "/:user" [user]
    (GET "/" [] not-implemented)
    (GET "/settings" [] not-implemented)
    (GET "/profile" [] not-implemented)))

(def project-routes
  (context "/:user/:project" [user project]
    (GET "/" [] (str user "/" project))
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
       (v/intro-guest) (v/intro-guest)))
   (POST "/" [] "post test")
   (POST "/sign-up" [email password]
     (println (str email ", " password))
     (v/sign-up-wait-confirm))
   (GET "/logout" [] "로그아웃처리")
   (GET "/throw" [] (throw (RuntimeException. "스택트레이스 실험")))
   (GET "/terms-of-service" [] v/terms-of-service)
   (GET "/privacy-policy" [] v/privacy-policy)
   admin/admin-routes
   user-routes
   project-routes))

(def app
  (routes
   (route/resources "/")
   (wrap-defaults web-routes site-defaults)
   (wrap-defaults smart-http-routes api-defaults)
   (route/not-found "Page not found")))

(def templates
  (routes
   (GET "/templates" []
     (v/layout {:title "템플릿 확인"}
               [:div.container
                [:div.row
                 [:ul.list-group
                  [:li.list-group-item
                   [:a {:href "/templates/sign-up-2"} "가입2단계"]]]]]))
   (GET "/templates/sign-up-2" [] (v/sign-up-wait-confirm))))

(def app-dev
  (-> (routes templates app)
      (wrap-exceptions)
      (wrap-reload)
      ))

(println (str *ns* " reloaded"))
