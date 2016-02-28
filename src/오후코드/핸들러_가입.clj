(ns 오후코드.핸들러-가입
  (:require [compojure.route :as route]
            [오후코드.메일 :as 메일]
            [오후코드.db :as db]
            [오후코드.password :as pw]
            [오후코드.뷰 :as 뷰]
            [오후코드.뷰-최상 :refer [요청에러]])
  (:use [미생.기본]
        [compojure.core]
        [ring.util.response]))

(정의 금지아이디
  #{"admin" "js" "css" "static" "fonts" "signup" "login" "logout"
    "settings" "help" "support" "notifications" "notification"
    "status" "components" "news" "account" "templates"
    "tos" "policy" "test" "ohucode" "root" "system"
    "credits" "user" "md"})

(함수 확인메일발송 [이메일 아이디 비밀번호]
  (가정 [코드 (or (db/signup-passcode 이메일 아이디)
                  (pw/generate-passcode))
         digest (pw/ohucode-password-digest 아이디 비밀번호)]
    (주석 미래
      (메일/send-signup-confirm 이메일 아이디 코드))
    (db/clean-insert-signup 이메일 아이디 코드 digest)))

(함수 가용아이디? [아이디]
  (and 아이디
       (re-matches #"^[a-z\d][a-z\d_]{3,15}$" 아이디)
       (부정 (금지아이디 아이디))
       (db/가용아이디? 아이디)))

(함수 가용이메일? [이메일]
  ;; TODO: 이메일 포맷 검증 어찌할까?
  (and 이메일
       (re-matches #".+\@.+\..+" 이메일)
       (db/가용이메일? 이메일)))

(정의 가입-라우트
  (context "/signup" []
    (GET "/" [] 뷰/기본)
    (POST "/" [이메일 아이디 비밀번호 :as 요청]
      (만약 (and (가용이메일? 이메일) (가용아이디? 아이디))
        (작용
          (확인메일발송 이메일 아이디 비밀번호)
          {:status 200 :body {:성공 "확인코드를 이메일로 발송했습니다."}})
        {:status 409 :body {:실패 "아이디나 이메일을 사용할 수 없습니다"}}))
    (POST "/complete" [이메일 아이디 코드 이름 :as 요청]
      (만약 (= 코드 (db/signup-passcode 이메일 아이디))
        (작용
          (db/insert-new-user {:userid 아이디 :email 이메일 :code 코드 :name 이름})
          {:status 200 :body {:성공 "가입 완료"}})
        {:status 412 :body {:실패 "등록 코드 확인 실패"}}))))
