(ns ohucode.route
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [ohucode.view :as v]
            [ohucode.state :refer [app-state]]))

(defroute "/" []
  (js/console.log "route / called")
  (swap! app-state assoc :page v/welcome-guest))

(defroute "/terms-of-service" []
  (js/console.log "route /terms-of-service called")
  (swap! app-state assoc :page v/terms-of-service))

(defroute "/privacy-policy" []
  (js/console.log "/privacy-policy route called")
  (swap! app-state assoc :page v/privacy-policy))

(defroute "/credits" []
  (js/console.log "/credits route called")
  (swap! app-state assoc :page v/credits))
