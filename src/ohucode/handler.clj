(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [taoensso.timbre :as timbre]
            [aleph.http :as http]
            [ohucode.view :as view]
            [ohucode.git :as git]
            [ohucode.git-http :refer [smart-http-routes]])
  (:import [java.util Locale]))

(defn- not-implemented [req]
  (throw (UnsupportedOperationException.)))

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
   (GET "/" [] "리로드?")
   (POST "/" [] "post test")
   user-routes
   project-routes))

(def app
  (routes
   (wrap-defaults web-routes
                  (update site-defaults
                          :session merge
                          {:store (cookie-store "ohucode passkey")}))
   (wrap-defaults smart-http-routes api-defaults)
   (route/resources "/")
   (route/not-found "Page not found")))

(def app-dev
  (-> app
      (wrap-reload)
      (wrap-lint)
      (wrap-with-logger)))

(defn start []
  (let [port 10000]
    (Locale/setDefault Locale/US)
    (timbre/info (str "Starting http-server on " port))
    (let [handler-for-reload #(app-dev %)]
      (http/start-server handler-for-reload {:port port}))))
