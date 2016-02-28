(ns ohucode.route
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [ohucode.top :refer [손님첫페이지 개인정보보호정책 서비스이용약관 감사의말]]
            [ohucode.signup :as 가입]
            [ohucode.state :refer [앱상태]]))

(defroute "/" []
  (js/console.log "route / called")
  (swap! 앱상태 assoc :페이지 손님첫페이지))

(defroute "/tos" []
  (swap! 앱상태 assoc :페이지 서비스이용약관))

(defroute "/policy" []
  (swap! 앱상태 assoc :페이지 개인정보보호정책))

(defroute "/credits" []
  (swap! 앱상태 assoc :페이지 감사의말))

(defroute "/signup" []
  (swap! 가입/가입상태 assoc :단계 1)
  (swap! 앱상태 assoc :페이지 가입/신청1))
