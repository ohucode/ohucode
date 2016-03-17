(ns ohucode.main
  (:require [reagent.core :as r]
            [ohucode.handler]
            [ohucode.top :refer [앱페이지]]
            [ohucode.route :as route]
            [re-frame.core :refer [dispatch]]))

(defn ^:export main []
  (aset js/marked.options "highlight"
        (fn [code] (.-value (.highlightAuto js/hljs code))))
  (r/render-component [앱페이지] (.getElementById js/document "app")))

(defn 온로드-등록 [함수]
  (let [이전 (aget js/window "onload")]
    (aset js/window "onload"
          (fn []
            (if 이전 (이전))
            (함수)))))

(온로드-등록 main)

(defn ^:export fig-reload []
  (js/console.log "리로드")
  (main))

(defn ^:export 로그인-알림 [이용자]
  (온로드-등록 (fn []
                 (js/console.log 이용자)
                 (dispatch [:로그인 (js->clj 이용자 :keywordize-keys true)]))))
