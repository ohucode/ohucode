(ns ohucode.main
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [cljsjs.marked]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]))

(enable-console-print!)

(defonce app-state (r/atom {}))
(defonce signup-state (r/atom {}))

(defn- on-change [key]
  (fn [e]
    (swap! signup-state assoc key (.-target.value e))))

(defn form-group [props]
  [:div.form-group
   [:label.control-label.col-sm-3 (:label props)]
   [:div.col-sm-9 [:input.form-control (dissoc props :label)]]])

(defn signup-form []
  [:form.form-horizontal
   [form-group {:label "이메일" :type "email" :name "이메일" :value (:email @signup-state)
                :placeholder "username@yourmail.net" :autofocus true
                :on-change (on-change :email)}]
   [form-group {:label "아이디" :type "text" :placeholder "userid" :name "아이디"
                :value (:userid @signup-state) :on-change (on-change :userid)}]
   [form-group {:label "비밀번호" :type "password" :placeholder "********"
                :name "비밀번호" :value (:password @signup-state)
                :on-change (on-change :password)}]
   [:div (:email @signup-state) ", " (:userid @signup-state) ", " (:password @signup-state)]])


(if-let [sf (.getElementById js/document "signup-form")]
  (r/render-component [signup-form] sf))

(.setOptions js/marked #js {"highlight"
                            (fn [code] (.-value (.highlightAuto js/hljs code)))})

(-> (js/$ "[data-markdown]")
    (.html (fn [idx text] (js/marked text))))

#_(.initHighlightingOnLoad js/hljs)
