(ns ohucode.main
  (:require [reagent.core :as r]
            [ohucode.top :refer [앱페이지]]
            [ohucode.route :as route]))

(defn ^:export main []
  (aset js/marked.options "highlight"
        (fn [code] (.-value (.highlightAuto js/hljs code))))
  (-> (js/$ "[data-markdown]")
      (.html (fn [idx text] (js/marked text))))
  (r/render-component [앱페이지] (.getElementById js/document "app")))

(main)
