(ns ohucode.signup
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST 서비스명 다음버튼 링크 입력컨트롤 알림-div
                                  prevent-default 패널]]
            [ohucode.state :refer [앱상태]]))

(defonce 가입상태 (r/atom {}))
(defonce 검증상태 (r/atom {}))

(add-watch 가입상태
           :검증
           (fn [key ref 이전 새값]
             (letfn [(유효? [키 vfn]
                       (if-let [값 (새값 키)]
                         (boolean (vfn 값))))
                     (검증! [키 vfn]
                       (let [유효? (유효? 키 vfn)]
                         (swap! 검증상태 assoc 키 유효?)
                         유효?))]
               (swap! 검증상태
                      assoc :전체
                      (every? true?
                              [(검증! :이메일 (partial re-matches #".+@.+\..+"))
                               (검증! :아이디 (partial re-matches
                                                       #"[가-힣a-z0-9][가-힣a-z0-9_\-]{3,15}"))
                               (검증! :성명 (partial re-matches #"[가-힝\w]{2,5}"))
                               (검증! :비밀번호 #(<= 7 (count %)))])))))

(defn 신청폼 [& 선택]
  (let [알림 (r/atom {})
        대기 (r/atom false)
        입력 (fn [상태키 속성]
               [입력컨트롤 (merge {:type "text" :placeholder (name 상태키)
                                   :value (상태키 @가입상태)
                                   :on-change #(swap! 가입상태 assoc 상태키
                                                      (.-target.value %))}
                                  속성)])
        입력그룹 (fn [상태키 입력속성]
                   [:div.form-group {:class (case (@검증상태 상태키)
                                              true "has-success"
                                              false "has-error"
                                              "")}
                    [입력 상태키 입력속성]])
        신청 (fn [e]
               (reset! 대기 true)
               (POST "/signup"
                   {:내용 (select-keys @가입상태
                                       [:아이디 :이메일 :비밀번호 :성명])
                    :성공 (fn [내용]
                            (dispatch [:가입 (:아이디 @가입상태)]))
                    :실패 (fn [코드 내용]
                            (reset! 알림 내용))
                    :완료 #(reset! 대기 false)}))]
    (fn [속성]
      [패널 [[:i.fa.fa-user-plus] " 가입 신청"]
       [:form
        (if (@알림 :실패)
          [알림-div :warning (:실패 @알림)])
        [:fieldset {:disabled @대기}
         [입력그룹 :이메일   {:type "email" :auto-focus true :auto-complete "email"}]
         [입력그룹 :아이디   {:auto-complete "username"}]
         [입력그룹 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [입력그룹 :성명     {:auto-complete "name"}]
         [:div.form-group [다음버튼 {:라벨 "가입" :대기 @대기
                                     :disabled (not (:전체 @검증상태))
                                     :class "btn-block btn-lg"
                                     :on-click (prevent-default 신청)}]]
         [:div.form-group [:div.text-center "가입하면 오후코드의 "
                           [링크 {:href "/tos"} "약관"] " 및 "
                           [링크 {:href "/policy"} "개인정보 취급방침"] "에 동의하시게 됩니다."]]]]])))

(defn 환영페이지 []
  [:div
   [:div.page-header [:h3 "환영합니다"]]
   [:div "이제 무얼 할 수 있나요?"]])
