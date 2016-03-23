(ns 오후코드.기본
  (:use [미생.기본])
  (:require [ring.util.codec :refer [url-encode]]))

(정의 ^:dynamic
  ^{:doc "클라이언트 IP"}
  *클라이언트IP* "0.0.0.0")

(정의 ^:dynamic
  ^{:doc "로그인한 사용자 정보"}
  *세션이용자*)

(정의 서비스명 "오후코드")

(함수 서비스명+ [& strs]
  (적용 str (concat 서비스명 " " strs)))

(함수 세션이용자 [요청]
  (get-in 요청 [:session :이용자]))

(함수 세션이용자-아이디 [요청]
  (get-in 요청 [:session :이용자 :아이디]))

(정의 로그인? (합성 부정 공? 세션이용자))

(함수 관리자? [요청]
  (= "admin" (:아이디 (세션이용자 요청))))

(함수 지금시각 []
  (quot (System/currentTimeMillis) 1000))
