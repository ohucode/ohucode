(ns ohucode.handler-signup
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ohucode.auth :as a]
            [ohucode.mail :as mail]
            [ohucode.db :as db]
            [ohucode.password :as password])
  (:use [ohucode.view-signup]))

(defn request-confirm-mail [nickname email]
  (let [code (password/random-6-digits)
        digest (password/random-digest)]
    (println code digest))
  )

(def signup-routes
  (context "/signup" []
    (POST "/" [email password]
        (println (str email ", " password))
        (sign-up-wait-confirm nil))))

(println (str *ns* " reloaded"))
