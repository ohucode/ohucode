(ns ohucode.signup
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [ohucode.core :refer [POST 서비스명 다음버튼 링크 입력컨트롤]]
            [ohucode.state :refer [앱상태]]))

(defonce ^:private 가입상태
  (r/atom {:이메일 "" :아이디 "" :비밀번호 ""
           :단계 1}))

(defonce ^:private 검증상태 (r/atom {}))

(add-watch 가입상태
           :검증
           (fn [key ref prev new]
             (letfn [(유효? [키 vfn]
                       (if-let [값 (get new 키)]
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

(declare 가입페이지)

(defn- 폼그룹 [속성 & 입력부]
  [:div.form-group (dissoc 속성 :label)
   [:label.control-label.col-sm-3 (:label 속성)]
   (into [:div.col-sm-9] 입력부)])

(defn- 유효성-클래스 [키]
  (case (@검증상태 키)
    true "has-success"
    false "has-error"
    ""))

(defn- on-change [키]
  (fn [e] (swap! 가입상태 assoc 키 (.-target.value e))))

(let [폼상태 (r/atom {:기다림 false})]
  (defn 신청1 []
    (let [set-userid-by-email
          (fn [e]
            (if (empty? (:아이디 @가입상태))
              (swap! 가입상태 assoc :아이디
                     (-> @가입상태 :이메일 (.split "@") first))))]
      [:form.form-horizontal
       [:fieldset {:disabled (:기다림 @폼상태)}
        (폼그룹 {:label "이메일" :class (유효성-클래스 :이메일)}
                [입력컨트롤 {:type "email" :name "이메일" :value (:이메일 @가입상태)
                             :auto-focus true
                             :auto-complete "email"
                             :placeholder "이메일 주소"
                             :on-change (on-change :이메일)
                             :on-blur set-userid-by-email}])
        (폼그룹 {:label "아이디" :class (유효성-클래스 :아이디)}
                [입력컨트롤 {:type "text" :placeholder "사용할 아이디" :name "아이디"
                             :value (:아이디 @가입상태)
                             :auto-complete "username"
                             :on-change (on-change :아이디)}])
        (폼그룹 {:label "비밀번호" :class (유효성-클래스 :비밀번호)}
                [입력컨트롤 {:type "password" :placeholder "사용할 비밀번호"
                             :name "비밀번호" :value (:비밀번호 @가입상태)
                             :auto-complete "current-password"
                             :on-change (on-change :비밀번호)}])
        (폼그룹 {} [다음버튼 {:disabled (not (:신청1 @검증상태))
                              :기다림 (:기다림 @폼상태)
                              :on-click (fn [e]
                                          (.preventDefault e)
                                          (swap! 폼상태 assoc :기다림 true)
                                          (js/setTimeout (fn []
                                                           (swap! 폼상태 dissoc :기다림)
                                                           (swap! 앱상태 assoc :페이지 가입페이지)
                                                           (swap! 가입상태 assoc :단계 2))
                                                         500)
                                          (POST "/signup"
                                              {:data (pr-str @가입상태)
                                               :success #()
                                               :failure #()}))}])]])))

(let [폼상태 (r/atom {:기다림 false})]
  (defn 신청2 "가입 2단계: 메일 확인코드 입력" []
    [:div
     [:form.form-horizontal
      [:fieldset {:disabled (:기다림 @폼상태)}
       (폼그룹 {:label "이메일"} [:div.form-control-static (:이메일 @가입상태)])
       (폼그룹 {:label "아이디"} [:div.form-control-static (:아이디 @가입상태)])
       (폼그룹 {:label "확인코드" :class (유효성-클래스 :코드)}
               [입력컨트롤 {:name "code" :type "text" :value (:코드 @가입상태)
                            :on-change (on-change :코드) :placeholder "메일에 적힌 6자리 숫자"
                            :auto-focus true}])
       (폼그룹 {} (다음버튼 {:disabled (not (:코드 @검증상태))
                             :기다림 (:기다림 @폼상태)
                             :on-click (fn [e]
                                         (.preventDefault e)
                                         (swap! 폼상태 assoc :기다림 true)
                                         (js/setTimeout (fn []
                                                          (swap! 폼상태 dissoc :기다림)
                                                          (swap! 가입상태 assoc :단계 3))
                                                        1000))})
               " "
               [:button.btn.btn-info {:title "확인 메일 재발송 요청하기"}
                "재발송 " [:i.fa.fa-send]])
       (:anti-forgery-field @가입상태)]]
     [:div.alert.alert-info.text-center
      "보내드린 메일에 있는 " [:strong "확인코드 "] "6자리 숫자를 입력해주세요. "
      (str @가입상태)
      (str @검증상태)]]))

(let [폼상태 (r/atom {})]
  (defn 신청3
    "기본 프로필 입력"
    []
    [:form.form-horizontal
     (폼그룹 {:label "이메일"} [:div.form-control-static (:이메일 @가입상태)])
     (폼그룹 {:label "아이디"} [:div.form-control-static (:아이디 @가입상태)])
     (폼그룹 {:label "이름"} [:input.form-control
                              {:type "text" :name "이름" :placeholder "홍길동"
                               :auto-focus true :auto-complete "name"}])
     (폼그룹 {} (다음버튼 {}))]))

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

(defn 가입페이지 []
  [:div.container.narrow-container
   [:div.page-header
    [:h2 [:i.fa.fa-user-plus] " 회원가입 "
     [:small (가입단계명 (dec (:단계 @가입상태)))]]]
   [:div.row
    [:div.col-sm-8
     [:div.panel.panel-ohucode
      [:div.panel-body (case (:단계 @가입상태)
                         1 [신청1]
                         2 [신청2]
                         3 [신청3]
                         "에러"
                         )]]]
    [:div.col-sm-4 [진행단계]]]])
