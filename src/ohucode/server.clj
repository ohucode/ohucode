(ns ohucode.server
  (:require [aleph.http :as http]
            [ohucode.handler :refer [app-dev]]
            [taoensso.timbre :as timbre]
            [clojure.tools.nrepl.server :refer [start-server stop-server]])
  (:import [java.util Locale]))

(defn start []
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

(defn stop []
  (.close http-server)
  (stop-server repl-server))
