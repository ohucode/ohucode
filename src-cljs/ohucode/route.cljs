(ns ohucode.route
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [ohucode.state :refer [앱상태]]))

(defroute "/" []
  (js/console.log "route / called")
  (swap! 앱상태 assoc :페이지 :첫페이지))

(defroute "/tos" []
  (swap! 앱상태 assoc :페이지 :이용약관))

(defroute "/policy" []
  (swap! 앱상태 assoc :페이지 :개인정보취급방침))

(defroute "/credits" []
  (swap! 앱상태 assoc :페이지 :감사의말))
