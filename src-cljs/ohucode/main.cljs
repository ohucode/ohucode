(ns ohucode.main
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce app-state (r/atom {}))
(defonce signup-state (r/atom {}))

(defn- on-change [key]
  (fn [e]
    (swap! signup-state assoc key (.-target.value e))))

(defn signup-form []
  (letfn [(fg [라벨 & 입력부]
            [:div.form-group
             [:label.control-label.col-sm-3 라벨]
             [:div.col-sm-9 입력부]])]
    [:form#signup-form-react.form-horizontal {:method "POST" :action "/signup"}
     (fg "이메일"
         [:input.form-control
          {:type "email" :name "이메일" :value (:email @signup-state)
           :placeholder "username@yourmail.net" :autofocus true
           :on-change (on-change :email)}])
     (fg "아이디"
         [:input.form-control
          {:type "text" :placeholder "userid" :name "아이디" :value (:userid @signup-state)
           :on-change (on-change :userid)}])
     (fg "비밀번호"
         [:input.form-control
          {:type "password" :placeholder "********" :name "비밀번호" :value (:password @signup-state)
           :on-change (on-change :password)}])
     [:div (:email @signup-state) ", " (:userid @signup-state) ", " (:password @signup-state)]]))

(r/render-component [signup-form]
                    (.getElementById js/document "app"))
