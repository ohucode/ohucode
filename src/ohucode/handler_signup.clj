(ns ohucode.handler-signup
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ohucode.auth :as a]
            [ohucode.mail :as mail]
            [ohucode.db :as db]
            [ohucode.password :as password])
  (:use [ohucode.view-signup]))

(def restricted-userids
  #{"admin" "js" "css" "static" "fonts" "signup" "login" "logout"
    "settings" "help" "support" "notifications" "notification"
    "status" "components" "news" "account" "templates"
    "terms-of-service" "privacy-policy" "test" "ohucode" "root" "system"})

(defn request-confirm-mail [email userid]
  (let [code (password/generate-passcode)]
    (comment future
      (mail/send-signup-confirm email userid code))
    (db/clean-insert-signup email userid code)))

(defn userid-acceptable? [userid]
  (and userid
       (re-matches #"^[a-z\d][a-z\d_]{3,15}$" userid)
       (not (contains? restricted-userids userid))
       (db/userid-acceptable? userid)))

(defn email-acceptable? [email]
  ;; TODO: 이메일 포맷 검증 어찌할까?
  (and email
       (re-matches #".+\@.+\..+" email)
       (db/email-acceptable? email)))

(def signup-routes
  (context "/signup" []
    (GET "/" [] signup-step1)
    (GET "/userid/:userid" [userid]
      {:status (if (userid-acceptable? userid) 200 409)})
    (GET "/email/:email" [email]
      {:status (if (email-acceptable? email) 200 409)})
    (POST "/" [email userid :as req]
      (if (and (email-acceptable? email)
               (userid-acceptable? userid))
        (do
          (request-confirm-mail email userid)
          (signup-step2 email userid))
        (do
          (-> (response (signup-step1 req))
              (assoc-in [:session :_flash] "이메일 주소나 아이디를 사용할 수 없습니다.")))))
    (POST "/2" [email userid code :as req]
      (if (and (email-acceptable? email)
               (userid-acceptable? userid)
               (= code (db/signup-passcode email userid)))
        (signup-step3 email userid code)
        ({:status 403 :body "등록 코드 확인 실패"})))
    (POST "/3" [email userid password code username :as req]
      (if (and (email-acceptable? email)
               (userid-acceptable? userid)
               (= code (db/signup-passcode email userid)))
        (do
          (db/insert-new-user {:userid userid :email email
                               :password password :code code
                               :name username})))
      (signup-step4 email userid))))
