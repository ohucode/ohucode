(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.lint :refer [wrap-lint]]
            [aleph.http :as http]
            [manifold.stream :as s]
            [ohucode.view :as view]
            [ohucode.git-http :as git-http])
  (:import [java.util Locale]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/chunked" []
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (java.io.ByteArrayInputStream. (.getBytes "test"))})
  (POST "/" []
    )
  (context "/:user/:project" [user project]
    (GET "/" [] (str user "/" "project"))
    (GET "/info/refs" [service]
      (if-not (contains? #{"git-receive-pack" "git-upload-pack"} service)
        {:status 403 :body "not a valid service request"}
        (-> (response "OK")
            (content-type "x-git-receive-pack-advertisement")))))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (wrap-defaults app-routes api-defaults))

(defn start []
  (Locale/setDefault Locale/US)
  (http/start-server
    (-> app
        (wrap-reload)
        (wrap-stacktrace)
        (wrap-lint))
    {:port 10000}))
