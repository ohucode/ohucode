(ns ohucode.handler-signup
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ohucode.auth :as a]
            [ohucode.mail :as mail]
            [ohucode.db :as db]
            [ohucode.password :as password])
  (:use [ohucode.view-signup]))

(def ^:private restricted-userids
  #{"admin" "js" "css" "static" "fonts" "signup" "login" "logout"
    "settings" "help" "support" "notifications" "notification"
    "status" "components" "news" "account" "templates"
    "terms-of-service" "privacy-policy" "test" "ohucode" "root" "system"})

(defn request-confirm-mail [email userid]
  (comment let [code (password/generate-passcode)]
    (future
      (mail/send-signup-confirm email userid passcode))
    (db/insert-or-update-signup email userid passcode)))

(defn userid-acceptable? [userid]
  (and (re-matches #"^[a-z\d][a-z\d_]{3,15}$" userid)
       (not (contains? restricted-userids userid))
       (db/userid-acceptable? userid)))

(defn email-acceptable? [email]
  ;; TODO: 이메일 포맷 검증 어찌할까?
  (and (re-matches #".+\@.+\..+" email)
       (db/email-acceptable? email)))

(def signup-routes
  (context "/signup" []
    (GET "/userid/:userid" [userid]
      {:status (if (userid-acceptable? userid) 200 409)})
    (GET "/email/:email" [email]
      {:status (if (email-acceptable? email) 200 409)})
    (POST "/" [email userid :as req]
      (or (and (email-acceptable? email)
               (userid-acceptable? userid))
          )
      (request-confirm-mail email userid)
      (signup-step2 email userid))
    (POST "/2" [email userid :as req]
      (signup-step3 email userid))
    (POST "/3" [email userid :as req]
      (signup-step4 email userid))))
