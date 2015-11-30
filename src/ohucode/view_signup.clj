(ns ohucode.view-signup
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]))

;; HTML5 validation과 vuejs와 bootstrap의 form validation css를 어떻게 잘 조립할지 고민중
(defn sign-up-form [_]
  [:form#sign-up-form {:method "POST" :action "/sign-up" :novalidate true}
   [:div.form-group.has-feedback {:v-bind:class "email | validate_class valid_email"}
    [:label {:for "sign-up-email"} "이메일"]
    [:input#sign-up-email.form-control
     {:type "email" :v-model "email"
      :placeholder "username@yourmail.net" :autofocus true}]
    [:span.glyphicon.glyphicon-remove.form-control-feedback
     {:v-bind:class "!valid_email" :aria-hidden "true"}]]

   [:div.form-group {:v-bind:class "nickname_class"}
    [:label {:for "sign-up-nickname"} "사용할 아이디"]
    [:input#sign-up-nickname.form-control
     {:type "text" :placeholder "영문숫자혼합 아이디" :v-model "nickname"}]]
   (anti-forgery-field)
   [:button.btn.btn-primary {:type "submit" :disabled "{{!valid_form}}"} "가입신청"]])

(defn sign-up-wait-confirm [_]
  (layout {:title "오후코드 가입 > 2단계"}
          [:div.row
           [:div.col-sm-3
            [:ul.list-group
             [:li.list-group-item "1. 가입"]
             [:li.list-group-item "2. 이메일 주소 확인"]]]
           [:div.col-sm-9
            [:ol.breadcrumb
             [:li "가입"]
             [:li "이메일 입력"]
             [:li "이메일 확인"]]]]))

(println (str *ns* " reloaded"))
