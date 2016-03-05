(ns ohucode.state
  (:require [reagent.core :as r]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :refer [dispatch]])
  (:import goog.history.Html5History))

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
