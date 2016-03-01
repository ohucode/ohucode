(ns ohucode.signup
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST 서비스명 다음버튼 링크 입력컨트롤 알림-div prevent-default]]
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

(defn- 폼그룹 [속성 & 입력부]
  (into [:div.form-group (dissoc 속성 :라벨)] 입력부))

(defn- 유효성-클래스 [키]
  (case (@검증상태 키)
    true "has-success"
    false "has-error"
    ""))

(defn- 변경 [키]
  (fn [e] (swap! 가입상태 assoc 키 (.-target.value e))))

(defn 신청폼 [& 선택]
  (let [알림 (r/atom {})
        대기 (r/atom false)
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
      [:div.panel.panel-signup>div.panel-body
       [:div.page-header [:h4 [:i.fa.fa-user-plus] " 가입 신청"]]
       [:form
        (if (@알림 :실패)
          [알림-div :warning (:실패 @알림)])
        [:fieldset {:disabled @대기}
         (폼그룹 {:라벨 "이메일" :class (유효성-클래스 :이메일)}
                 [입력컨트롤 {:type "email" :name "이메일" :value (:이메일 @가입상태)
                              :auto-focus true :auto-complete "email"
                              :placeholder "이메일" :on-change (변경 :이메일)}])
         (폼그룹 {:라벨 "아이디" :class (유효성-클래스 :아이디)}
                 [입력컨트롤 {:type "text" :placeholder "아이디" :name "아이디"
                              :value (:아이디 @가입상태) :auto-complete "username"
                              :on-change (변경 :아이디)}])
         (폼그룹 {:라벨 "성명" :class (유효성-클래스 :성명)}
                 [입력컨트롤 {:type "text" :placeholder "성명"
                              :auto-complete "name"
                              :value (:성명 @가입상태)
                              :on-change (변경 :성명)}])
         (폼그룹 {:라벨 "비밀번호" :class (유효성-클래스 :비밀번호)}
                 [입력컨트롤 {:type "password" :placeholder "비밀번호"
                              :name "비밀번호" :value (:비밀번호 @가입상태)
                              :auto-complete "current-password"
                              :on-change (변경 :비밀번호)}])
         (폼그룹 {} [다음버튼 {:라벨 "가입"
                               :disabled (not (:전체 @검증상태))
                               :class "btn-block btn-lg"
                               :대기 @대기
                               :on-click (prevent-default 신청)}])
         (폼그룹 {} [:div.text-center "가입하면 오후코드의 "
                     [링크 {:href "/tos"} "약관"] " 및 "
                     [링크 {:href "/policy"} "개인정보 취급방침"] "에 동의하시게 됩니다."])]]])))
