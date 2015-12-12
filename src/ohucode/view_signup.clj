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
     {:type "email" :v-model "email.value" :name "email"
      :placeholder "username@yourmail.net" :autofocus true}]]
   [:div.form-group {:v-bind:class "userid | validation_class"}
    [:label.control-label {:for "signup-userid"} "사용할 아이디"]
    [:input#signup-userid.form-control
     {:type "text" :placeholder "userid" :name "userid" :v-model "userid.value"}]]
   (anti-forgery-field)
   [:div.form-group.pull-right
    [:button.btn.btn-primary {:type "submit" :disabled "{{!valid_form}}"} "다음 >"]]])

(def ^:private signup-step-texts
  ["아이디/이메일 입력"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(defn- signup-progress [active-step]
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector (iterate inc 1) signup-step-texts)]
     [:li.disabled {:class (if (= step active-step) "active" "disabled")}
      [:a {:href "#"} (str step ". " text)]])])

(defn- signup-layout [active-step _ & body]
  (let [title (brand-name+ "가입 > " active-step "단계")]
    (layout {:title title}
            [:div.container
             [:div.row
              [:div.page-header [:h1 title]]]
             [:div.row
              [:div.col-sm-3 (signup-progress active-step)]
              [:div.col-sm-9
               [:div.row
                [:h2 (signup-step-texts (dec active-step))]
                body]]]])))

(defn signup-step1 [_]
  "가입 1단계: 아이디와 이메일 접수"
  (signup-layout 1 _
                 [:div.row
                  [:div.col-sm-7
                   (signup-form _)]]))

(defn signup-step2 [req]
  "가입 2단계: 메일 확인코드 입력"
  (signup-layout 2 req
                 [:div.panel.panel-default
                  [:div.panel-body
                   [:form#confirm-form.form-horizontal
                    [:div.form-group
                     [:label.control-label.col-xs-3.col-sm-2 "이메일"]
                     [:div.form-control-static.col-xs-6.col-sm-10 "hatemogi@gmail.com"]]
                    [:div.form-group
                     [:label.control-label.col-xs-3.col-sm-2 {:for "confirm-code"} "확인코드"]
                     [:div.col-xs-6.col-sm-3
                      [:div.input-group
                       [:input#confirm-code.form-control {:v-model "code" :type "text"
                                                          :placeholder "######" :autofocus true}]
                       [:span.input-group-btn [:button.btn.btn-primary "확인"]]]]]]]]
                 [:p "위 이메일 주소로 확인 코드를 보냈습니다. 보내드린 메일에 적혀있는 6자리 "
                  [:strong "확인코드"]
                  "를 입력해주세요."]
                 [:p req]
                 [:p.text-right "다른 이메일 주소로 가입하시겠어요? > "
                  [:a {:href "#"} "1단계에서 다시 시작"]]))

(defn signup-step3 [_]
  "기본 프로필 입력"
  (signup-layout 3 _
                 [:div.row
                  [:div.col-sm-7
                   (signup-form _)]]))
