(ns ohucode.handler-signup
  (:require [compojure.route :as route]
            [ohucode.mail :as mail]
            [ohucode.db :as db]
            [ohucode.password :as password]
            [ohucode.view-top :as v-top])
  (:use [misaeng.core]
        [compojure.core]
        [ring.util.response]
        [ohucode.view-signup]))

(정의 restricted-userids
  #{"admin" "js" "css" "static" "fonts" "signup" "login" "logout"
    "settings" "help" "support" "notifications" "notification"
    "status" "components" "news" "account" "templates"
    "terms-of-service" "privacy-policy" "test" "ohucode" "root" "system"
    "credits"})

(함수 request-confirm-mail [email userid]
  (가정 [code (or (db/signup-passcode email userid)
                  (password/generate-passcode))]
    (주석 future
      (mail/send-signup-confirm email userid code))
    (db/clean-insert-signup email userid code)))

(함수 userid-acceptable? [userid]
  (and userid
       (re-matches #"^[a-z\d][a-z\d_]{3,15}$" userid)
       (not (contains? restricted-userids userid))
       (db/userid-acceptable? userid)))

(함수 email-acceptable? [email]
  ;; TODO: 이메일 포맷 검증 어찌할까?
  (and email
       (re-matches #".+\@.+\..+" email)
       (db/email-acceptable? email)))

(정의 signup-routes
  (context "/signup" []
    (GET "/" [] signup-step1)
    (GET "/userid/:userid" [userid]
      {:status (if (userid-acceptable? userid) 200 409)})
    (GET "/email/:email" [email]
      {:status (if (email-acceptable? email) 200 409)})
    (POST "/" [email userid :as req]
      (만약 (and (email-acceptable? email)
               (userid-acceptable? userid))
        (묶음
          (request-confirm-mail email userid)
          (signup-step2 req email userid))
        (묶음
          (-> (redirect "/signup")
              (assoc-in [:session :_flash] "이메일 주소나 아이디를 사용할 수 없습니다.")))))
    (POST "/2" [email userid code :as req]
      (if (and (email-acceptable? email)
               (userid-acceptable? userid)
               (= code (db/signup-passcode email userid)))
        (signup-step3 req email userid code)
        (v-top/request-error req "등록 코드 확인 실패")))
    (POST "/3" [email userid password code username :as req]
      (만약 (and (email-acceptable? email)
                 (userid-acceptable? userid)
                 (= code (db/signup-passcode email userid)))
        (묶음
          (db/insert-new-user {:userid userid :email email
                               :password password :code code
                               :name username})
          (signup-step4 req email userid))
        (v-top/request-error "파라미터 오류")))))
