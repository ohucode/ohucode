(ns ohucode.session
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch]]
            [ohucode.core :refer [POST 다음버튼 링크 입력컨트롤 prevent-default
                                  패널 검증반응 유효-클래스]]))

(defonce 폼상태 (r/atom {}))

(defn 로그인폼 [속성]
  (let [키목록 [:아이디 :비밀번호]
        검증상태 (검증반응 폼상태 키목록)
        입력 (fn [키 입력속성]
               [:div.form-group (유효-클래스 (@검증상태 키))
                [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (@폼상태 키)
                                    :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                   입력속성)]])
        로그인 (fn [e]
                 (swap! 폼상태 assoc :대기 true)
                 (POST "/user/login"
                     {:내용 (select-keys @폼상태 키목록)
                      :성공 (fn [내용] (dispatch [:로그인 (:이용자 내용)]))
                      :실패 (fn [코드 내용] (js/console.log 코드 내용))
                      :완료 #(swap! 폼상태 dissoc :대기)}))]
    (fn [속성]
      [패널 [[:i.fa.fa-sign-in] " 로그인"]
       [:form
        [:fieldset {:disabled (:대기 @폼상태)}
         [입력 :아이디 {:auto-focus true :auto-complete "username"
                        :placeholder "아이디 또는 이메일"}]
         [입력 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [:div.form-group [다음버튼 {:라벨 "로그인" :대기 (:대기 @폼상태)
                                     :disabled (:무효 @검증상태)
                                     :class "btn-block"
                                     :on-click (prevent-default 로그인)}]]]]])))
