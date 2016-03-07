(ns ohucode.user
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ohucode.core :refer [다음버튼 링크 입력컨트롤
                                  패널 검증반응 유효-클래스 알림-div]]))

(defn 가입폼 [& 선택]
  (let [fg :div.form-group
        키목록 [:이메일 :아이디 :비밀번호 :성명]
        폼상태 (r/atom {})
        검증상태 (검증반응 폼상태 키목록)
        신청상태 (subscribe [:가입신청])
        입력 (fn [키 속성]
               [fg (유효-클래스 (@검증상태 키))
                [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (@폼상태 키)
                                    :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                   속성)]])]
    (fn [속성]
      [패널 [[:i.fa.fa-user-plus] " 가입 신청"]
       (if-let [실패 (@신청상태 :실패)]
         [알림-div :warning 실패])
       [:form
        [:fieldset {:disabled (@신청상태 :로딩?)}
         [입력 :이메일   {:type "email" :auto-focus true :auto-complete "email"}]
         [입력 :아이디   {:auto-complete "username"}]
         [입력 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [입력 :성명     {:auto-complete "name"}]
         [fg [다음버튼 {:라벨 "가입" :로딩? (@신청상태 :로딩?)
                        :disabled (@검증상태 :무효)
                        :class "btn-block btn-lg"
                        :클릭 #(dispatch [:가입신청 (select-keys @폼상태 키목록)])}]]
         [fg [:div.text-center "가입하면 오후코드의 "
              [링크 {:href "/tos"} "약관"] " 및 "
              [링크 {:href "/policy"} "개인정보 취급방침"] "에 동의하시게 됩니다."]]]]])))

(defn 가입환영 []
  [:div
   [:div.page-header [:h3 "환영합니다"]]
   [:div "이제 무얼 할 수 있나요?"]])

(defn 로그인폼 []
  (let [fg :div.form-group
        폼상태 (r/atom {})
        키목록 [:아이디 :비밀번호]
        검증상태 (검증반응 폼상태 키목록)
        입력 (fn [키 속성]
               [fg (유효-클래스 (@검증상태 키))
                [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (@폼상태 키)
                                    :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                   속성)]])
        로그인상태 (subscribe [:로그인])]
    (fn []
      [패널 [[:i.fa.fa-sign-in] " 로그인"]
       [:form
        [:fieldset {:disabled (@로그인상태 :로딩?)}
         [입력 :아이디 {:auto-focus true :auto-complete "username"
                        :placeholder "아이디 또는 이메일"}]
         [입력 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [fg [다음버튼 {:라벨 "로그인" :로딩? (@로그인상태 :로딩?)
                        :disabled (@검증상태 :무효)
                        :class "btn-block"
                        :클릭 #(dispatch [:로그인 (select-keys @폼상태 키목록)])}]]]]])))

(defn 이용자홈 []
  [:div "로그인한 이용자 홈 화면"])
