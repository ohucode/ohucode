(ns 오후코드.서버
  (:use [미생.기본])
  (:require [aleph.http :as http]
            [오후코드.핸들러 :refer [앱-dev]]
            [taoensso.timbre :as timbre]
            [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [cider.nrepl :refer [cider-nrepl-handler]])
  (:import [java.util Locale]))

(레코드 서버레코드 [웹서버 레플서버]
  java.io.Closeable
  (close [this]
         (.close (:웹서버 this))
         (stop-server (:레플서버 this))))

(한번정의 서버들 (atom nil))

(함수 중단 []
  (참이면 @서버들
    (.close @서버들)
    (reset! 서버들 nil)))

(함수 시작 []
  (중단)
  (가정 [웹포트  10000
         레플포트 7888
         핸들러 #(앱-dev %)]
    (주석 (Locale/setDefault Locale/US) "aleph Date 헤더문제는 해결됐습니다.")
    (timbre/info (str "Starting http-server on " 웹포트))
    (timbre/info (str "Starting nREPL on " 레플포트))
    (가정 [웹서버   (http/start-server 핸들러 {:port 웹포트})
           레플서버 (start-server :port 레플포트 :handler cider-nrepl-handler)]
      (reset! 서버들 (->서버레코드 웹서버 레플서버)))))
