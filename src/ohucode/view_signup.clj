(ns ohucode.view-signup
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]))

;; HTML5 validation과 vuejs와 bootstrap의 form validation css를 어떻게 잘 조립할지 고민중
(defn signup-form [_]
  [:form#signup-form {:method "POST" :action "/signup" :novalidate true}
   [:div.form-group.has-feedback {:v-bind:class "class_email"}
    [:label {:for "signup-email"} "이메일"]
    [:input#signup-email.form-control
     {:type "email" :v-model "email.value"
      :placeholder "username@yourmail.net" :autofocus true}]
    [:span.glyphicon.form-control-feedback
     {:v-bind:class "class_email | feedback_class" :aria-hidden "true"}]]
   [:div.form-group {:v-bind:class "class_nickname"}
    [:label {:for "signup-nickname"} "사용할 아이디"]
    [:input#signup-nickname.form-control
     {:type "text" :placeholder "your_id" :v-model "nickname.value"}]]
   (anti-forgery-field)
   [:button.btn.btn-primary {:type "submit" :disabled "{{!valid_form}}"} "가입신청"]])

(defn- signup-progress [active-step]
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector
                          (iterate inc 1)
                          ["아이디/이메일 입력"
                           "이메일 주소 확인"
                           "기본 프로필"
                           "이용약관 동의"])]
     [:li {:class (if (= step active-step) "active")}
      [:a {:href "#"} (str step ". " text)]])])

(defn signup-step1 [_]
  "가입 1단계: 아이디와 이메일 접수"
  (layout {:title (brand-name+ "가입")}))

(defn signup-step2 [_]
  "가입 2단계: 이메일 확인"
  (layout {:title (brand-name+ "가입 > 2단계")}
          [:div.container
           [:div.row
            [:div.col-sm-3 (signup-progress 2)]
            [:div.col-sm-9
             [:div.page-header [:h1 (brand-name+ "가입 신청")]]
             [:div.page-header [:h2 "아이디/이메일 입력"]]
             (signup-form _)
             [:div.page-header [:h2 "이메일 주소 확인"]]
             [:p "입력하신 이메일 주소로 확인 메일을 보내드렸습니다. 메일을 확인하셔서 "
              [:strong "가입확인코드"]
              "를 아래에 입력해주세요."]
             [:div.row
              [:div.col-sm-6.col-md-4
               [:form#confirm-form
                [:div.input-group.input-group-lg
                 [:input.form-control.input-lg {:type "text" :placeholder "######" :autofocus true}]
                 [:span.input-group-btn.input-group-lg
                  [:button.btn.btn-primary "확인"]]]]]]
             
             ]]]))

(println (str *ns* " reloaded"))
