(ns ohucode.view-signup
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]))

;; HTML5 validation과 vuejs와 bootstrap의 form validation css를 어떻게 잘 조립할지 고민중
(defn signup-form [_]
  [:form#signup-form {:method "POST" :action "/signup" :novalidate true}
   [:div.form-group {:v-bind:class "email | validation_class"}
    [:label.control-label {:for "signup-email"} "이메일"]
    [:input#signup-email.form-control
     {:type "email" :v-model "email.value"
      :placeholder "username@yourmail.net" :autofocus true}]]
   [:div.form-group {:v-bind:class "userid | validation_class"}
    [:label.control-label {:for "signup-userid"} "사용할 아이디"]
    [:input#signup-userid.form-control
     {:type "text" :placeholder "userid" :v-model "userid.value"}]]
   (anti-forgery-field)
   [:div.form-group.pull-right
    [:button.btn.btn-primary {:type "submit" :disabled "{{!valid_form}}"} "다음 >"]]])

(defn- signup-progress [active-step]
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector
                          (iterate inc 1)
                          ["아이디/이메일 입력"
                           "이메일 주소 확인"
                           "기본 프로필"
                           "이용약관 동의"])]
     [:li.disabled {:class (if (= step active-step) "active" "disabled")}
      [:a {:href "#"} (str step ". " text)]])])

(defn signup-step1 [_]
  "가입 1단계: 아이디와 이메일 접수"
  (layout {:title (brand-name+ "가입")}
          [:div.container
           [:div.row
            [:div.page-header [:h1 (brand-name+ "가입 신청")]]]
           [:div.row
            [:div.col-sm-3 (signup-progress 1)]
            [:div.col-sm-9
             
             [:h2 "아이디/이메일 입력"]
             [:div.row
              [:div.col-sm-6
               (signup-form _)]]]]]))

(defn signup-step2 [_]
  "가입 2단계: 이메일 확인"
  (layout {:title (brand-name+ "가입 > 2단계")}
          [:div.container
           [:div.row
            [:div.col-sm-3 (signup-progress 2)]
            [:div.col-sm-9
             [:div.page-header [:h1 (brand-name+ "가입 신청")]]
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
