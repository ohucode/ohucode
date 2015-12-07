(ns ohucode.handler-signup
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ohucode.auth :as a]
            [ohucode.mail :as mail]
            [ohucode.db :as db]
            [ohucode.password :as password])
  (:use [ohucode.view-signup]))

(defn request-confirm-mail [userid email]
  (comment let [code (password/generate-passcode)]
    (future
      (mail/send-signup-confirm userid email passcode))
    (db/insert-signup userid email passcode)))

(def signup-routes
  (context "/signup" []
    (POST "/" [userid email]
      (request-confirm-mail userid email)
      (sign-up-wait-confirm nil))))

(println (str *ns* " reloaded"))
