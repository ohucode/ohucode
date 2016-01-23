(ns 오후코드.서버
  (:require [aleph.http :as http]
            [오후코드.핸들러 :refer [app-dev]]
            [taoensso.timbre :as timbre]
            [clojure.tools.nrepl.server :refer [start-server stop-server]])
  (:import [java.util Locale]))

(defn 시작 []
  (defonce http-server
    (let [port 10000]
      (Locale/setDefault Locale/US)
      (timbre/info (str "Starting http-server on " port))
      (let [handler-for-reload #(app-dev %)]
        (http/start-server handler-for-reload {:port port}))))
  (defonce repl-server
    (let [port 7888]
      (timbre/info (str "Starting nREPL on " port))
      (start-server :port port))))

(defn 중단 []
  (.close http-server)
  (stop-server repl-server))
