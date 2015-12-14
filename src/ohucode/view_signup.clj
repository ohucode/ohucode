(ns ohucode.view-signup
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]))

(defn- next-btn [attrs]
  [:button.btn.btn-primary attrs "다음 " [:i.fa.fa-angle-double-right]])

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
         (next-btn {:type "submit" :disabled "{{!valid_form}}"}))]))

(def ^:private signup-step-texts
  ["아이디/이메일 입력"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(defn- signup-progress [active-step]
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector (iterate inc 1) signup-step-texts)]
     [:li {:class (if (= step active-step) "active" "disabled")}
      [:a {:href "#"} (str step ". " text)]])])

(defn- signup-layout [active-step _ & body]
  (let [title (brand-name+ "가입 > " active-step "단계")]
    (layout {:title title}
            [:div.container.signup-container
             [:div.page-header
              [:h2 [:i.fa.fa-user-plus] " 회원가입 "
               [:small (signup-step-texts (dec active-step))]]]
             [:div.row
              [:div.col-sm-8 [:div.panel.panel-signup [:div.panel-body body]]]
              [:div.col-sm-4 (signup-progress active-step)]]])))

(defn signup-step1 [_]
  "가입 1단계: 아이디와 이메일 접수"
  (signup-layout 1 _
                 (signup-form _)))

(defn signup-step2 [req]
  "가입 2단계: 메일 확인코드 입력"
  (letfn [(fg [label-text & input-section]
            [:div.form-group
             [:label.control-label.col-sm-3 label-text]
             [:div.col-sm-9 input-section]])]
    (signup-layout 2 req
                   [:form#confirm-form.form-horizontal
                    (fg "이메일" [:div.form-control-static "hatemogi@gmail.com"])
                    (fg "확인코드" [:input#confirm-code.form-control
                                    {:v-model "code" :type "text"
                                     :placeholder "######" :autofocus true}])
                    (fg ""
                        (next-btn {})
                        [:div.pull-right
                         [:button.btn.btn-info "재발송 " [:i.fa.fa-send]]
                         " "
                         [:button.btn.btn-success "처음부터 " [:i.fa.fa-undo]]])
                    (anti-forgery-field)])))

(defn signup-step3 [req]
  "기본 프로필 입력"
  (letfn [(fg [label-text & input-section]
            [:div.form-group
             [:label.control-label.col-sm-3 label-text]
             [:div.col-sm-9 input-section]])]
    (signup-layout 3 req
                   [:form#signup-profile-form.form-horizontal {:method "POST" :action "/me"}
                    (fg "아이디" [:div.form-control-static "hatemogi"])
                    (fg "이름" [:input.form-control {:type "text" :placeholder "홍길동" :autofocus true}])
                    (fg "비밀번호" [:input.form-control {:type "password" :placeholder "********"}])
                    (fg "비번확인" [:input.form-control {:type "password" :placeholder "********"}])
                    (fg "" (next-btn {}))])))
