(ns ohucode.handler-signup
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ohucode.auth :as a]
            [ohucode.db :as db])
  (:use [ohucode.view-signup]))

(def signup-routes
  (context "/signup" []
    (POST "/" [email password]
        (println (str email ", " password))
        (sign-up-wait-confirm nil))))

(println (str *ns* " reloaded"))
