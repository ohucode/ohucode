(ns ohucode.signup
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST 다음버튼 링크 입력컨트롤 알림-div
                                  prevent-default 패널 검증반응 유효-클래스]]
            [ohucode.state :refer [앱상태]]))

(defonce 폼상태 (r/atom {}))

(defn 신청폼 [& 선택]
  (let [알림 (r/atom {})
        키목록 [:이메일 :아이디 :비밀번호 :성명]
        검증상태 (검증반응 폼상태 키목록)
        입력 (fn [키 속성]
               [:div.form-group (유효-클래스 (@검증상태 키))
                [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (키 @폼상태)
                                    :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                   속성)]])
        신청 (fn [e]
               (swap! 폼상태 assoc :대기 true)
               (POST "/signup"
                   {:내용 (select-keys @폼상태 키목록)
                    :성공 (fn [내용] (dispatch [:가입성공 (:아이디 @폼상태)]))
                    :실패 (fn [코드 내용] (reset! 알림 내용))
                    :완료 #(swap! 폼상태 dissoc :대기)}))]
    (fn [속성]
      [패널 [[:i.fa.fa-user-plus] " 가입 신청"]
       [:form
        (if (@알림 :실패)
          [알림-div :warning (:실패 @알림)])
        [:fieldset {:disabled (@폼상태 :대기)}
         [입력 :이메일   {:type "email" :auto-focus true :auto-complete "email"}]
         [입력 :아이디   {:auto-complete "username"}]
         [입력 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [입력 :성명     {:auto-complete "name"}]
         [:div.form-group [다음버튼 {:라벨 "가입" :대기 (@폼상태 :대기)
                                     :disabled (:무효 @검증상태)
                                     :class "btn-block btn-lg"
                                     :on-click (prevent-default 신청)}]]
         [:div.form-group [:div.text-center "가입하면 오후코드의 "
                           [링크 {:href "/tos"} "약관"] " 및 "
                           [링크 {:href "/policy"} "개인정보 취급방침"] "에 동의하시게 됩니다."]]]]])))

(defn 환영페이지 []
  [:div
   [:div.page-header [:h3 "환영합니다"]]
   [:div "이제 무얼 할 수 있나요?"]])
