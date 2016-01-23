(ns ohucode.core
  (:use [미생.기본]))

(정의 ^:dynamic
  ^{:doc "클라이언트 IP"}
  *client-ip* "0.0.0.0")

(정의 ^:dynamic
  ^{:doc "로그인한 사용자 정보"}
  *signed-user*)

(정의 서비스명 "오후코드")

(함수 서비스명+ [& strs]
  (적용 str (concat 서비스명 " " strs)))

(정의 ^:dynamic
  ^{:doc "가입 인증코드 발급후 유효시간 (단위: 초)"}
  *passcode-expire-sec* (* 30 60))

(함수 session-user [요청]
  (get-in 요청 [:session :user]))

(정의 로그인? (조합 부정 공? session-user))

(함수 관리자? [요청]
  (= "admin" (:userid (session-user 요청))))

(함수 wrap-user-info [핸들러]
  (fn [요청]
    (binding [*signed-user* (session-user 요청)]
      (핸들러 요청))))
