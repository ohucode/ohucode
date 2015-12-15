(ns ohucode.handler-signup
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ohucode.auth :as a]
            [ohucode.mail :as mail]
            [ohucode.db :as db]
            [ohucode.password :as password])
  (:use [ohucode.view-signup]))

(defn request-confirm-mail [email userid]
  (comment let [code (password/generate-passcode)]
    (future
      (mail/send-signup-confirm email userid passcode))
    (db/insert-or-update-signup email userid passcode)))

(def signup-routes
  (context "/signup" []
    (POST "/" [email userid :as req]
      (request-confirm-mail email userid)
      (signup-step2 req))
    (POST "/2" [email userid :as req]
      (request-confirm-mail email userid)
      (signup-step2 email userid))
    (POST "/3" [email userid :as req]
      (request-confirm-mail email userid)
      (signup-step3 email userid))))
