(ns ohucode.main
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defonce app-state (atom {}))

(defn signup-form []
  [])
(reagent/render-component [signup-form]
                          (.getElementById js/document "app")))
