(ns ohucode.core
  (:use [misaeng.core]))

(정의 ^:dynamic
  ^{:doc "클라이언트 IP"}
  *client-ip* "0.0.0.0")

(정의 ^:dynamic
  ^{:doc "로그인한 사용자 정보"}
  *signed-user*)

(정의 서비스명 "오후코드")

(함수 서비스명+ [& strs]
  (apply str (concat 서비스명 " " strs)))

(정의 ^:dynamic
  ^{:doc "가입 인증코드 발급후 유효시간 (단위: 초)"}
  *passcode-expire-sec* (* 30 60))

(함수 session-user [req]
  (get-in req [:session :user]))

(정의 signed-in? (comp not nil? session-user))

(함수 관리자? [req]
  (= "admin" (:userid (session-user req))))

(함수 wrap-user-info [handler]
  (fn [req]
    (binding [*signed-user* (session-user req)]
      (handler req))))
