(ns ohucode.route
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [re-frame.core :refer [dispatch]]))

(defroute "/" []
  (js/console.log "route / called")
  (dispatch [:페이지 :첫페이지>가입]))

(defroute "/tos" []
  (dispatch [:페이지 :이용약관]))

(defroute "/policy" []
  (dispatch [:페이지 :개인정보취급방침]))

(defroute "/credits" []
  (dispatch [:페이지 :감사의말]))
