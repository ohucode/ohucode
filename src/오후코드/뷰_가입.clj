(ns 오후코드.뷰-가입
  (:require [taoensso.timbre :as timbre])
  (:use [미생.기본]
        [오후코드.기본]
        [오후코드.뷰]
        [hiccup.core]
        [hiccup.page]))

;; HTML5 validation과 vuejs와 bootstrap의 form validation css를 어떻게 잘 조립할지 고민중

(함수 다음버튼 [& whatever])
(함수 가입양식1 [& whatever])

(정의 ^:private signup-step-texts
  ["아이디/이메일 입력"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(함수- signup-progress [active-step]
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector (iterate inc 1) signup-step-texts)]
     [:li.disabled {:class (if (= step active-step) "active")}
      [:a {:href "#"} text]])])

(함수- 가입-레이아웃 [요청 active-step form & body]
  (가정 [제목 (서비스명+ "가입 > " active-step "단계")]
    (레이아웃 요청
              {:title 제목}
              [:div.container.narrow-container
               [:div.page-header
                [:h2 [:i.fa.fa-user-plus] " 회원가입 "
                 [:small (signup-step-texts (감소 active-step))]]]
               [:div.row
                [:div.col-sm-8
                 [:div.panel.panel-ohucode [:div.panel-body form]]
                 body]
                [:div.col-sm-4 (signup-progress active-step)]]])))


(함수 가입-1단계 [요청]
  "가입 1단계: 아이디와 이메일 접수"
  (가입-레이아웃 요청 1 (가입양식1 요청)))



(함수 가입-3단계 [요청 이메일 아이디 코드]
  "기본 프로필 입력"
  (가정함 [(fg [라벨 & 입력부]
               [:div.form-group
                [:label.control-label.col-sm-3 라벨]
                [:div.col-sm-9 입력부]])]
      (가입-레이아웃 요청 3
                     [:form#signup-profile-form.form-horizontal {:method "POST" :action "/signup/3"}
                      (fg "이메일" [:div.form-control-static 이메일])
                      (fg "아이디" [:div.form-control-static 아이디])
                      (fg "이름" [:input.form-control
                                  {:type "text" :name "username" :placeholder "홍길동" :autofocus true}])
                      (fg "비밀번호"
                          [:input.form-control
                           {:type "password" :v-model "password" :name "password" :placeholder "********"}])
                      (fg "비번확인"
                          [:input.form-control
                           {:type "password" :v-model "password2" :placeholder "********"}])
                      (fg "" (다음버튼 {}))
                      [:input {:type "hidden" :name "code" :value 코드}]
                      [:input {:type "hidden" :name "email" :value 이메일}]
                      [:input {:type "hidden" :name "userid" :value 아이디}]
                      (anti-forgery-field)])))

(함수 가입-4단계 [요청 이메일 아이디]
  "이용약관 동의"
  (가정함 [(fg [라벨 & 입력부]
            [:div.form-group
             [:label.control-label 라벨]
             입력부])]
    (가입-레이아웃 요청 4
     [:form#signup-profile-orm.form {:method "POST" :action "/signup/4"}
      (fg "이용약관" [:textarea.form-control {:rows 10 :cols 80} "사이트 이용약관 \n블라블라"])
      [:div.checkbox [:label
                      [:input {:type "checkbox" :name "agree" :value "true"}]
                      "이용약관에 동의합니다"]]
      (fg ""
          [:button.btn.btn-warning.pull-right "가입취소"]
          (다음버튼 {:disabled ""}))
      (anti-forgery-field)])))
