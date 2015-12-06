(ns ohucode.view-signup
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]))

;; HTML5 validation과 vuejs와 bootstrap의 form validation css를 어떻게 잘 조립할지 고민중
(defn sign-up-form [_]
  [:form#sign-up-form {:method "POST" :action "/sign-up" :novalidate true}
   [:div.form-group.has-feedback {:v-bind:class "class_email"}
    [:label {:for "sign-up-email"} "이메일"]
    [:input#sign-up-email.form-control
     {:type "email" :v-model "email.value"
      :placeholder "username@yourmail.net" :autofocus true}]
    [:span.glyphicon.form-control-feedback
     {:v-bind:class "class_email | feedback_class" :aria-hidden "true"}]]
   [:div.form-group {:v-bind:class "class_nickname"}
    [:label {:for "sign-up-nickname"} "사용할 아이디"]
    [:input#sign-up-nickname.form-control
     {:type "text" :placeholder "your_id" :v-model "nickname.value"}]]
   (anti-forgery-field)
   [:button.btn.btn-primary {:type "submit" :disabled "{{!valid_form}}"} "가입신청"]])

(defn sign-up-wait-confirm [_]
  (layout {:title "오후코드 가입 > 2단계"}
          [:div.container
           [:div.row
            [:div.col-sm-3
             [:ul.nav.nav-pills.nav-stacked
              (for [stage (map str
                               (iterate inc 1)
                               (repeat ". ")
                               ["아이디/이메일 입력"
                                "이메일 주소 확인"
                                "필수 정보"
                                "추가 정보"
                                "이용약관 동의"])]
                [:li.active [:a {:href "#"} stage]])]]
            [:div.col-sm-9
             [:div.page-header [:h1 "오후코드 가입 신청"]]
             [:div.page-header [:h2 "아이디/이메일 입력"]]
             (sign-up-form _)
             [:div.page-header [:h2 "이메일 주소 확인"]]
             [:p "입력하신 이메일 주소로 확인 메일을 보내드렸습니다. 메일을 확인하셔서 "
              [:strong "가입확인코드"]
              "를 아래에 입력해주세요."]
             [:div.row
              [:div.col-sm-6.col-md-4
               [:form#confirm-form
                [:div.input-group
                 [:input.form-control {:type "text" :placeholder "######" :autofocus true}]
                 [:span.input-group-btn
                  [:button.btn.btn-primary "확인"]]]]]]
             
             [:div.row [:div.col-sm-12
                        [:button.btn.btn-success "또 보내기"]
                        " "
                        [:button.btn.btn-default "다시 입력하기"]]]
             [:div.page-header [:h2 "필수 정보"]]
             [:div.page-header [:h2 "추가 정보"]]
             [:div.page-header [:h2 "이용약관 동의"]]]]]))

(println (str *ns* " reloaded"))
