(ns ohucode.session
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch]]
            [ohucode.core :refer [POST 다음버튼 링크 입력컨트롤 prevent-default 패널 검증함수]]
            [ohucode.state :refer [앱상태]]))

(defonce 폼상태 (r/atom {}))

(defn 로그인폼 [속성]
  (let [검증상태 (reaction
                  (let [검증 (fn [키] (if-let [값 (@폼상태 키)] (boolean ((검증함수 키) 값))))
                        아이디 (검증 :아이디)
                        비밀번호 (검증 :비밀번호)]
                    {:아이디 아이디
                     :비밀번호 비밀번호
                     :전체 (and 아이디 비밀번호)}))
        입력그룹 (fn [키 입력속성]
                   [:div.form-group {:class (case (@검증상태 키)
                                              true "has-success"
                                              false "has-error"
                                              "")}
                    [입력컨트롤 (merge {:type "text" :placeholder (name 키)
                                        :value (@폼상태 키)
                                        :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                       입력속성)]])
        로그인 (fn [e]
                 (swap! 폼상태 :대기 true)
                 (POST "/user/login"
                     {:내용 (select-keys @폼상태 [:아이디 :비밀번호])
                      :성공 (fn [내용] (dispatch [:로그인 (:아이디 @폼상태)]))
                      :실패 (fn [코드 내용] (js/console.log 코드 내용))
                      :완료 #(swap! 폼상태 dissoc :대기)}))]
    (fn [속성]
      [패널 [[:i.fa.fa-sign-in] " 로그인"]
       [:form
        [:fieldset {:disabled (:대기 @폼상태)}
         [입력그룹 :아이디 {:auto-focus true :auto-complete "username"
                            :placeholder "아이디 또는 이메일"}]
         [입력그룹 :비밀번호 {:type "password" :auto-complete "current-password"}]
         #_[:div (str @폼상태)]
         #_[:div (str @검증상태)]
         [:div.form-group [다음버튼 {:라벨 "로그인" :대기 (:대기 @폼상태)
                                     :disabled (not (:전체 @검증상태))
                                     :class "btn-block"
                                     :on-click (prevent-default 로그인)}]]]]])))
