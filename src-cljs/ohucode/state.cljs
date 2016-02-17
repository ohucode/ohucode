(ns ohucode.state
  (:require [reagent.core :as r]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))

(defonce app-state (r/atom {}))

(defonce signup-state (r/atom {}))

(defonce signup-valid-state (r/atom {}))

(add-watch signup-state
           :validation
           (fn [key ref prev new]
             (js/console.log (str key ": " prev ", " new))
             (js/console.log (str @signup-valid-state))
             (swap! signup-valid-state assoc :email true)))


(defonce history
  (doto (Html5History.)
    (.setPathPrefix  (str js/window.location.protocol "//" js/window.location.host))
    (.setUseFragment false)
    (.setEnabled true)
    (events/listen EventType/NAVIGATE
                   (fn [e]
                     (let [path (.-token e)]
                       (secretary/dispatch! (if (empty? path)
                                              js/window.location.pathname
                                              path)))))))
