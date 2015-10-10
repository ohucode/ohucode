(ns ohucode.server
  (:require [aleph.http :as http]
            [ohucode.handler :refer [app-dev]]
            [taoensso.timbre :as timbre])
  (:import [java.util Locale]))

(defn start []
  (def server (let [port 10000]
                (Locale/setDefault Locale/US)
                (timbre/info (str "Starting http-server on " port))
                (let [handler-for-reload #(app-dev %)]
                  (http/start-server handler-for-reload {:port port})))))
