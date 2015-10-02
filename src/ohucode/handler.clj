(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ohucode.view :as view]
            [ohucode.git-http :as git-http]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (context "/:user/:project" [user project]
    (GET "/" [] (str user "/" "project"))
    (GET "/info/refs" [service]
         (str "got refs req for service=" service))))

(def app
  (wrap-defaults app-routes site-defaults))
