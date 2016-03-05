(ns ohucode.main
  (:require [reagent.core :as r]
            [ohucode.handler]
            [ohucode.top :refer [앱페이지]]
            [ohucode.route :as route]))

(defn ^:export main []
  (aset js/marked.options "highlight"
        (fn [code] (.-value (.highlightAuto js/hljs code))))
  (r/render-component [앱페이지] (.getElementById js/document "app")))

(aset js/window "onload" main)

(defn ^:export fig-reload []
  (js/console.log "리로드")
  (main))
