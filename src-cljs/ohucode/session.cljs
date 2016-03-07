(ns ohucode.session
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ohucode.core :refer [다음버튼 링크 입력컨트롤 prevent-default
                                  패널 검증반응 유효-클래스]]))

(defonce 폼상태 (r/atom {}))

(defn 로그인폼 []
  (let [fg :div.form-group
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
                        :클릭
                        #(dispatch [:로그인 (select-keys @폼상태 키목록)])}]]]]])))
