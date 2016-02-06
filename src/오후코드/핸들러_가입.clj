(ns 오후코드.핸들러-가입
  (:require [compojure.route :as route]
            [오후코드.메일 :as 메일]
            [오후코드.db :as db]
            [오후코드.password :as password]
            [오후코드.뷰-최상 :refer [요청에러]])
  (:use [미생.기본]
        [오후코드.뷰-가입]
        [compojure.core]
        [ring.util.response]))

(정의 금지아이디
  #{"admin" "js" "css" "static" "fonts" "signup" "login" "logout"
    "settings" "help" "support" "notifications" "notification"
    "status" "components" "news" "account" "templates"
    "terms-of-service" "privacy-policy" "test" "ohucode" "root" "system"
    "credits"})

(함수 확인메일발송 [이메일 아이디]
  (가정 [코드 (or (db/signup-passcode 이메일 아이디)
                  (password/generate-passcode))]
    (주석 미래
      (메일/send-signup-confirm 이메일 아이디 코드))
    (db/clean-insert-signup 이메일 아이디 코드)))

(함수 가용아이디? [아이디]
  (and 아이디
       (re-matches #"^[a-z\d][a-z\d_]{3,15}$" 아이디)
       (부정 (contains? 금지아이디 아이디))
       (db/가용아이디? 아이디)))

(함수 가용이메일? [이메일]
  ;; TODO: 이메일 포맷 검증 어찌할까?
  (and 이메일
       (re-matches #".+\@.+\..+" 이메일)
       (db/가용이메일? 이메일)))

(정의 가입-라우트
  (context "/signup" []
    (GET "/" [] 가입-1단계)
    (GET "/userid/:userid" [userid]
      {:status (만약 (가용아이디? userid) 200 409)})
    (GET "/email/:email" [email]
      {:status (만약 (가용이메일? email) 200 409)})
    (POST "/" [이메일 아이디 :as 요청]
      (만약 (and (가용이메일? 이메일) (가용아이디? 아이디))
        (작용
          (확인메일발송 이메일 아이디)
          (가입-2단계 요청 이메일 아이디))
        (작용
          (-> (redirect "/signup")
              (assoc-in [:session :_flash] "이메일 주소나 아이디를 사용할 수 없습니다.")))))
    (POST "/2" [email userid code :as 요청]
      (만약 (and (가용이메일? email)
                 (가용아이디? userid)
                 (= code (db/signup-passcode email userid)))
        (가입-3단계 요청 email userid code)
        (요청에러 요청 "등록 코드 확인 실패")))
    (POST "/3" [email userid password code username :as 요청]
      (만약 (and (가용이메일? email) (가용아이디? userid)
                 (= code (db/signup-passcode email userid)))
        (작용
          (db/insert-new-user {:userid userid :email email
                               :password password :code code
                               :name username})
          (가입-4단계 요청 email userid))
        (요청에러 "파라미터 오류")))))
