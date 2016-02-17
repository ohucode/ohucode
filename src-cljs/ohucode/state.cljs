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
             (letfn [(valid? [key vfn]
                       (if-let [val (get new key)]
                         (if-not (empty? val)
                           (boolean (vfn val)))))
                     (set-valid! [key vfn]
                       (let [valid? (valid? key vfn)]
                         (swap! signup-valid-state assoc key valid?)
                         valid?))]
               (swap! signup-valid-state
                      assoc :form
                      (and
                       (set-valid! :email (partial re-matches #".+@.+\..+"))
                       (set-valid! :userid (partial re-matches #"[a-z0-9][a-z0-9_]{3,15}"))
                       (set-valid! :password #(< 6 (count %))))))))

;; FIXME: 크롬에서는 최초 NAVIGATE 이벤트 발생하지 않음.
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
