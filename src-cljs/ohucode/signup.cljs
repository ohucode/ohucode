(ns ohucode.signup
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [ohucode.core :refer
             [POST 서비스명 다음버튼 링크 입력컨트롤 알림-div prevent-default]]
            [ohucode.state :refer [앱상태]]))

(defonce ^:private 가입상태
  (r/atom {:이메일 "" :아이디 "" :비밀번호 ""
           :단계 1}))

(defonce ^:private 검증상태 (r/atom {}))

(add-watch 가입상태
           :검증
           (fn [key ref 이전 새값]
             (letfn [(유효? [키 vfn]
                       (if-let [값 (새값 키)]
                         (if-not (empty? 값)
                           (boolean (vfn 값)))))
                     (검증! [키 vfn]
                       (let [유효? (유효? 키 vfn)]
                         (swap! 검증상태 assoc 키 유효?)
                         유효?))]
               (swap! 검증상태
                      assoc :신청1
                      (and
                       (검증! :이메일 (partial re-matches #".+@.+\..+"))
                       (검증! :아이디 (partial re-matches #"[a-z0-9][a-z0-9_]{3,15}"))
                       (검증! :비밀번호 #(< 6 (count %)))))
               (swap! 검증상태
                      assoc :신청2
                      (true? (검증! :코드 (partial re-matches #"[0-9]{6}")))))))

(declare 신청2)
(declare 신청3)

(defn- 폼그룹 [속성 & 입력부]
  [:div.form-group (dissoc 속성 :label)
   [:label.control-label.col-sm-4 (:label 속성)]
   (into [:div.col-sm-8] 입력부)])

(defn- 유효성-클래스 [키]
  (case (@검증상태 키)
    true "has-success"
    false "has-error"
    ""))

(defn- 변경 [키]
  (fn [e] (swap! 가입상태 assoc 키 (.-target.value e))))

(def ^:private 가입단계명
  ["가입신청"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(defn- 진행단계 []
  (let [현단계 (:단계 @가입상태)]
    [:ul.nav.nav-pills.nav-stacked
     (for [[step text] (map vector (iterate inc 1) 가입단계명)]
       ^{:key step} [:li.disabled {:class (if (= step 현단계) "active")}
                     [:a {:href "#"} text]])]))

(defn- 레이아웃 [페이지]
  [:div.container.narrow-container
   [:div.page-header
    [:h2 [:i.fa.fa-user-plus] " 회원가입 "
     [:small (가입단계명 (dec (:단계 @가입상태)))]]]
   [:div.row
    [:div.col-sm-8
     [:div.panel.panel-ohucode
      [:div.panel-body 페이지]]]
    [:div.col-sm-4 [진행단계]]]])

(defn 신청폼 []
  (let [폼상태 (r/atom {})
        대기 (r/atom false)
        신청 (fn [e]
               (reset! 대기 true)
               (POST "/signup"
                   {:내용 (select-keys @가입상태
                                       [:아이디 :이메일 :비밀번호])
                    :성공 (fn [data]
                            (swap! 가입상태 assoc :단계 2)
                            (swap! 앱상태 assoc :페이지 신청2))
                    :실패 (fn [body]
                            (js/console.log body)
                            (swap! 폼상태 assoc :오류 {:설명 body}))
                    :완료 #(reset! 대기 false)}))]
    (fn [속성]
      [:form.form-horizontal
       (if-let [오류 (:오류 @폼상태)]
         [알림-div :warning (:설명 오류)])
       [:fieldset {:disabled @대기}
        (폼그룹 {:label "이메일" :class (유효성-클래스 :이메일)}
                [입력컨트롤 {:type "email" :name "이메일" :value (:이메일 @가입상태)
                             :auto-focus true :auto-complete "email"
                             :placeholder "이메일 주소" :on-change (변경 :이메일)
                             :on-blur (fn 아이디자동입력 [e]
                                        (if (empty? (:아이디 @가입상태))
                                          (swap! 가입상태 assoc :아이디
                                                 (-> @가입상태 :이메일 (.split "@") first))))}])
        (폼그룹 {:label "아이디" :class (유효성-클래스 :아이디)}
                [입력컨트롤 {:type "text" :placeholder "사용할 아이디" :name "아이디"
                             :value (:아이디 @가입상태) :auto-complete "username"
                             :on-change (변경 :아이디)}])
        (폼그룹 {:label "비밀번호" :class (유효성-클래스 :비밀번호)}
                [입력컨트롤 {:type "password" :placeholder "사용할 비밀번호"
                             :name "비밀번호" :value (:비밀번호 @가입상태)
                             :auto-complete "current-password"
                             :on-change (변경 :비밀번호)}])
        (폼그룹 {} [다음버튼 {:라벨 "무료 가입하기"
                              :disabled (not (:신청1 @검증상태))
                              :대기 @대기
                              :on-click (prevent-default 신청)}])]])))

(defn 신청1 []
  (레이아웃 [신청폼]))

(defn 신청2 []
  (let [폼상태 (r/atom {})
        대기 (r/atom false)
        코드확인 (fn [e]
                   (reset! 대기 true)
                   (POST "/signup/2"
                       {:내용 (select-keys @가입상태 [:이메일 :아이디 :코드])
                        :성공 (fn [내용]
                                (js/console.log 내용)
                                (swap! 앱상태 assoc :페이지 신청3))
                        :실패 (fn [코드 내용]
                                (js/console.log #js [코드 내용])
                                (swap! 폼상태 assoc :오류 {:설명 (:message 내용)}))
                        :완료 #(reset! 대기 false)}))]
    (fn [속성]
      (레이아웃 [:div
                 (if-let [오류 (:오류 @폼상태)]
                   [알림-div :warning (:설명 오류)])
                 [:form.form-horizontal
                  [:fieldset {:disabled @대기}
                   (폼그룹 {:label "이메일"} [:div.form-control-static (:이메일 @가입상태)])
                   (폼그룹 {:label "아이디"} [:div.form-control-static (:아이디 @가입상태)])
                   (폼그룹 {:label "확인코드" :class (유효성-클래스 :코드)}
                           [입력컨트롤 {:name "code" :type "text" :value (:코드 @가입상태)
                                        :on-change (변경 :코드) :placeholder "메일에 적힌 6자리 숫자"
                                        :auto-focus true}])
                   (폼그룹 {} (다음버튼 {:disabled (not (:코드 @검증상태))
                                         :대기 @대기
                                         :on-click (prevent-default 코드확인)})
                           " "
                           [:button.btn.btn-info {:title "확인 메일 재발송 요청하기"}
                            "재발송 " [:i.fa.fa-send]])
                   (:anti-forgery-field @가입상태)]]
                 [:div.alert.alert-info.text-center
                  "보내드린 메일에 있는 " [:strong "확인코드 "] "6자리 숫자를 입력해주세요. "]]))))

(defn 신청3 "기본 프로필 입력" []
  (let [폼상태 (r/atom {})
        대기 (r/atom false)
        입력 (fn [e]
               (reset! 대기 true)
               (POST "/signup/3"
                   {:내용 (select-keys @가입상태 [:이메일 :아이디 :코드 :이름])
                    :성공 (fn [내용]
                            (js/console.log 내용)
                            (swap! 가입상태 assoc :단계 3))
                    :실패 (fn [코드 내용]
                            (js/console.log #js [코드 내용])
                            (js/console.log 내용))
                    :완료 (fn [] (reset! 대기 false))}))]
    (fn [속성]
      [레이아웃
       [:form.form-horizontal
        [:fieldset {:disabled @대기}
         (폼그룹 {:label "이메일"} [:div.form-control-static (:이메일 @가입상태)])
         (폼그룹 {:label "아이디"} [:div.form-control-static (:아이디 @가입상태)])
         (폼그룹 {:label "이름"} [:input.form-control
                                  {:type "text" :placeholder "홍길동"
                                   :auto-focus true :auto-complete "name"
                                   :value (:이름 @가입상태)
                                   :on-change (변경 :이름)}])
         (폼그룹 {} (다음버튼 {:disabled @대기
                               :on-click (prevent-default 입력)}))]]])))
