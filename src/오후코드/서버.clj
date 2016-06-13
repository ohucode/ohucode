(ns 오후코드.서버
  (:require [aleph.http :as http]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.nrepl.server :refer [start-server
                                                stop-server]]
            [taoensso.timbre :as l]
            [미생.기본 :refer :all]
            [오후코드.핸들러 :refer [앱-dev]]))

(l/merge-config! {:ns-blacklist ["com.mchange.*"
                                 "io.netty.*"]})

(레코드 서버레코드 [웹서버 레플서버]
  java.io.Closeable
  (close [this]
         (.close (:웹서버 this))
         (stop-server (:레플서버 this))))

(한번정의 서버들 (atom nil))

(함수 중단! []
  (참이면 @서버들
    (.close @서버들)
    (reset! 서버들 nil)))

(함수 시작! []
  (가정 [웹포트  10000
         레플포트 7888
         핸들러 #(앱-dev %)]
    (l/info (str "Starting http-server on " 웹포트))
    (l/info (str "Starting nREPL on " 레플포트))
    (가정 [웹서버   (http/start-server 핸들러 {:port 웹포트})
           레플서버 (start-server :port 레플포트 :handler cider-nrepl-handler)]
      (reset! 서버들 (->서버레코드 웹서버 레플서버)))))
