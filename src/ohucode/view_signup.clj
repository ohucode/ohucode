(ns ohucode.view-signup
  (:require [taoensso.timbre :as timbre])
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]))

(defn- next-btn [attrs]
  [:button.btn.btn-primary
   (merge {:type "submit" }attrs) "다음 " [:i.fa.fa-angle-double-right]])

;; HTML5 validation과 vuejs와 bootstrap의 form validation css를 어떻게 잘 조립할지 고민중
(defn signup-form [_]
  (letfn [(fg [attrs label-text & input-section]
            [:div.form-group attrs
             [:label.control-label.col-sm-3 label-text]
             [:div.col-sm-9 input-section]])]
    [:form#signup-form.form-horizontal {:method "POST" :action "/signup" :novalidate true}
     (fg {:v-bind:class "email | validation_class"}
         "이메일"
         [:input#signup-email.form-control
          {:type "email" :v-model "email.value" :name "email"
           :placeholder "username@yourmail.net" :autofocus true
           :v-on:blur "email_change"}])
     (fg {:v-bind:class "userid | validation_class"}
         "아이디"
         [:input#signup-userid.form-control
          {:type "text" :placeholder "userid" :name "userid" :v-model "userid.value"}])
     (anti-forgery-field)
     (fg {} ""
         (next-btn {:disabled "{{!valid_form}}"}))]))

(def ^:private signup-step-texts
  ["아이디/이메일 입력"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(defn- signup-progress [active-step]
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector (iterate inc 1) signup-step-texts)]
     [:li.disabled {:class (if (= step active-step) "active")}
      [:a {:href "#"} text]])])

(defn- signup-layout [active-step form & body]
  (let [title (brand-name+ "가입 > " active-step "단계")]
    (layout {:title title}
            [:div.container.signup-container
             [:div.page-header
              [:h2 [:i.fa.fa-user-plus] " 회원가입 "
               [:small (signup-step-texts (dec active-step))]]]
             [:div.row
              [:div.col-sm-8
               [:div.panel.panel-signup [:div.panel-body form]]
               body]
              [:div.col-sm-4 (signup-progress active-step)]]])))


(defn signup-step1 [req]
  "가입 1단계: 아이디와 이메일 접수"
  (timbre/debug req)
  (signup-layout 1 (signup-form req)))

(defn signup-step2 [email userid]
  "가입 2단계: 메일 확인코드 입력"
  (letfn [(fg [label-text & input-section]
            [:div.form-group
             [:label.control-label.col-sm-3 label-text]
             [:div.col-sm-9 input-section]])]
    (signup-layout
     2
     [:form#signup-confirm-form.form-horizontal
      {:method "POST" :action "/signup/2"}
      (fg "이메일" [:div.form-control-static email])
      (fg "아이디" [:div.form-control-static userid])
      (fg "확인코드" [:input#confirm-code.form-control
                      {:v-model "code" :name "code" :type "text"
                       :placeholder "######" :autofocus true}])
      (fg "" (next-btn {:disabled "{{!valid_form}}"})
          " "
          [:button.btn.btn-info {:v-on:click "resend" :title "확인 메일 재발송 요청하기"
                                 :data-toggle "tooltip" :data-placement "top"}
           "재발송 " [:i.fa.fa-send]])
      [:input {:type "hidden" :v-model "email" :name "email" :value email}]
      [:input {:type "hidden" :v-model "userid" :name "userid" :value userid}]
      (anti-forgery-field)]
     [:div.alert.alert-info.text-center
      "보내드린 메일에 있는 " [:strong "확인코드 "] "6자리 숫자를 입력해주세요. "])))

(defn signup-step3 [email userid code]
  "기본 프로필 입력"
  (letfn [(fg [label-text & input-section]
            [:div.form-group
             [:label.control-label.col-sm-3 label-text]
             [:div.col-sm-9 input-section]])]
    (signup-layout
     3
     [:form#signup-profile-form.form-horizontal {:method "POST" :action "/signup/3"}
      (fg "이메일" [:div.form-control-static email])
      (fg "아이디" [:div.form-control-static userid])
      (fg "이름" [:input.form-control
                  {:type "text" :name "username" :placeholder "홍길동" :autofocus true}])
      (fg "비밀번호"
          [:input.form-control
           {:type "password" :v-model "password" :name "password" :placeholder "********"}])
      (fg "비번확인"
          [:input.form-control
           {:type "password" :v-model "password2" :placeholder "********"}])
      (fg "" (next-btn {}))
      [:input {:type "hidden" :name "code" :value code}]
      [:input {:type "hidden" :name "email" :value email}]
      [:input {:type "hidden" :name "userid" :value userid}]
      (anti-forgery-field)])))

(defn signup-step4 [email userid]
  "이용약관 동의"
  (letfn [(fg [label-text & input-section]
            [:div.form-group
             [:label.control-label label-text]
             input-section])]
    (signup-layout
     4
     [:form#signup-profile-orm.form {:method "POST" :action "/signup/4"}
      (fg "이용약관" [:textarea.form-control {:rows 10 :cols 80} "사이트 이용약관 \n블라블라"])
      [:div.checkbox [:label
                      [:input {:type "checkbox" :name "agree" :value "true"}]
                      "이용약관에 동의합니다"]]
      (fg ""
          [:button.btn.btn-warning.pull-right "가입취소"]
          (next-btn {:disabled ""}))
      (anti-forgery-field)])))
