(ns ohucode.main
  (:require [reagent.core :as r]
            [cljsjs.marked]
            [ohucode.handler]
            [ohucode.route]
            [ohucode.top :refer [앱페이지]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]))

(defn ^:export main []
  (aset js/marked.options "highlight"
        (fn [code] (.-value (.highlightAuto js/hljs code))))
  (dispatch-sync [:초기화])
  (r/render-component [앱페이지] (.getElementById js/document "app"))
  (secretary/dispatch! js/window.location.pathname))

(defn 온로드-등록 [함수]
  (let [이전 (aget js/window "onload")]
    (aset js/window "onload"
          (fn []
            (if 이전 (이전))
            (함수)))))

(온로드-등록 main)

(defn ^:export fig-reload []
  (js/console.log "리로드")
  (js/console.debug (clj->js @re-frame.db/app-db)))

(defn ^:export 로그인-알림 [이용자]
  (온로드-등록 (fn []
                 (js/console.log 이용자)
                 (dispatch [:로그인 (js->clj 이용자 :keywordize-keys true)]))))
