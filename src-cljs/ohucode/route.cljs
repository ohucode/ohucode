(ns ohucode.route
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [ohucode.top :refer [손님첫페이지 개인정보보호정책 서비스이용약관 감사의말]]
            [ohucode.state :refer [앱상태]]))

(defroute "/" []
  (js/console.log "route / called")
  (swap! 앱상태 assoc :page 손님첫페이지))

(defroute "/terms-of-service" []
  (js/console.log "route /terms-of-service called")
  (swap! 앱상태 assoc :page 서비스이용약관))

(defroute "/privacy-policy" []
  (js/console.log "/privacy-policy route called")
  (swap! 앱상태 assoc :page 개인정보보호정책))

(defroute "/credits" []
  (js/console.log "/credits route called")
  (swap! 앱상태 assoc :page 감사의말))
