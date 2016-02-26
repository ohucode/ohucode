(ns 오후코드.뷰-가입
  (:use [미생.기본]
        [오후코드.기본]
        [오후코드.뷰]))

(함수 가입-4단계 [요청 이메일 아이디]
  "이용약관 동의"
  (주석 가정함 [(fg [라벨 & 입력부]
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
