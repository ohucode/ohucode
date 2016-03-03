(ns ohucode.state
  (:require [reagent.core :as r]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]])
  (:import goog.history.Html5History))

(defonce 앱상태
  (r/atom {:알림 {}     ;; 요청 결과 성패 표시용도
           :이용자 {}   ;; 로그인 여부, 로그인한 이용자 기본 정보
           :페이지 nil  ;; 표시할 페이지 정보
           }))

;; FIXME: 크롬에서는 최초 NAVIGATE 이벤트 발생하지 않음.
(defonce 히스토리
  (doto (Html5History.)
    (.setPathPrefix (str js/window.location.protocol "//" js/window.location.host))
    (.setUseFragment false)
    (.setEnabled true)
    (events/listen EventType/NAVIGATE
                   (fn [e]
                     (let [path (.-token e)]
                       (secretary/dispatch! (if (empty? path)
                                              js/window.location.pathname
                                              path)))))))

(register-handler :가입성공
                  (fn [db [_ 값]]
                    (js/console.log ["이벤트 받았어요." 값])
                    db))
