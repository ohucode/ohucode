(ns 오후코드.서버
  (:require [aleph.http :as http]
            [오후코드.핸들러 :refer [app-dev]]
            [taoensso.timbre :as timbre]
            [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [cider.nrepl :refer [cider-nrepl-handler]])
  (:import [java.util Locale]))

(defonce servers (atom {}))

(defn 시작 []
  (let [http-port 10000
        repl-port 7888
        handler-for-reload #(app-dev %)]
    (do
      (Locale/setDefault Locale/US)
      (timbre/info (str "Starting http-server on " http-port))
      (timbre/info (str "Starting nREPL on " repl-port))
      (let [http-server (http/start-server handler-for-reload {:port http-port})
            repl-server (start-server :port repl-port :handler cider-nrepl-handler)]
        (swap! servers assoc :http-server http-server)
        (swap! servers assoc :repl-server repl-server)))))

(defn 중단 []
  (.close (:http-server @servers))
  (stop-server (:repl-server @servers)))
