(ns ohucode.main
  (:require [reagent.core :as r]
            [ohucode.view :as v]))

(defn ^:export main []
  (aset js/marked.options "highlight"
        (fn [code] (.-value (.highlightAuto js/hljs code))))
  (-> (js/$ "[data-markdown]")
      (.html (fn [idx text] (js/marked text))))
  (r/render-component [v/app-page] (.getElementById js/document "app")))

(main)
