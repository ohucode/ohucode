(ns ohucode.signup
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [ohucode.core :refer [POST 서비스명 다음버튼 링크 입력컨트롤]]
            [ohucode.state :refer [앱상태]]))

(defonce ^:private 가입상태 (r/atom {:이메일 "" :아이디 "" :비밀번호 ""}))
(defonce ^:private 검증상태 (r/atom {}))

(add-watch 가입상태
           :검증
           (fn [key ref prev new]
             (letfn [(valid? [key vfn]
                       (if-let [val (get new key)]
                         (if-not (empty? val)
                           (boolean (vfn val)))))
                     (set-valid! [key vfn]
                       (let [valid? (valid? key vfn)]
                         (swap! 검증상태 assoc key valid?)
                         valid?))]
               (swap! 검증상태
                      assoc :전체
                      (and
                       (set-valid! :이메일 (partial re-matches #".+@.+\..+"))
                       (set-valid! :아이디 (partial re-matches #"[a-z0-9][a-z0-9_]{3,15}"))
                       (set-valid! :비밀번호 #(< 6 (count %))))))))

(def ^:private 가입단계명
  ["아이디/이메일 입력"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(defn- 진행단계 []
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector (iterate inc 1) 가입단계명)]
     [:li.disabled {:class (if (= step 1) "active")}
      [:a {:href "#"} text]])])

(defn- 레이아웃 [폼 & 본문]
  [:div.container.narrow-container
   [:div.page-header
    [:h2 [:i.fa.fa-user-plus] " 회원가입 "
     [:small (가입단계명 (:step @가입상태))]]]
   [:div.row
    [:div.col-sm-8
     (into [:div.panel.panel-ohucode
            [:div.panel-body 폼]]
           본문)]
    [:div.col-sm-4 진행단계]]])

(declare 신청2)

(let [폼상태 (r/atom {:기다림 false})]
  (defn 신청1 []
    (let [fg (fn [속성 & 입력부]
               [:div.form-group (dissoc 속성 :label)
                [:label.control-label.col-sm-3 (:label 속성)]
                (into [:div.col-sm-9] 입력부)])
          on-change (fn [key]
                      (fn [e] (swap! 가입상태 assoc key (.-target.value e))))
          validity-class (fn [key]
                           (case (@검증상태 key)
                             true "has-success"
                             false "has-error"
                             ""))
          set-userid-by-email (fn [e]
                                (js/console.log "블러 불림" (:아이디 @가입상태))
                                (if (empty? (:아이디 @가입상태))
                                  (swap! 가입상태 assoc :아이디
                                         (-> @가입상태 :이메일 (.split "@") first))))]
      [:form.form-horizontal
       [:fieldset {:disabled (:기다림 @폼상태)}
        (fg {:label "이메일" :class (validity-class :이메일) :disabled true}
            [입력컨트롤 {:type "email" :name "이메일" :value (:이메일 @가입상태)
                            :auto-focus true
                            :auto-complete "email"
                            :placeholder "이메일 주소"
                            :on-change (on-change :이메일)
                            :on-blur set-userid-by-email}])
        (fg {:label "아이디" :class (validity-class :userid)}
            [입력컨트롤 {:type "text" :placeholder "사용할 아이디" :name "아이디"
                            :value (:아이디 @가입상태)
                            :auto-complete "username"
                            :on-change (on-change :아이디)}])
        (fg {:label "비밀번호" :class (validity-class :비밀번호)}
            [입력컨트롤 {:type "password" :placeholder "사용할 비밀번호"
                            :name "비밀번호" :value (:비밀번호 @가입상태)
                            :auto-complete "current-password"
                            :on-change (on-change :비밀번호)}])
        (fg {} [다음버튼 {:disabled (not (:전체 @검증상태))
                          :waiting (:기다림 @폼상태)
                          :on-click (fn [e]
                                      (.preventDefault e)
                                      (swap! 폼상태 assoc :기다림 true)
                                      (js/setTimeout #(do (swap! 폼상태 dissoc :기다림)
                                                          (swap! 앱상태 assoc :page [신청2]))
                                                     1000)
                                      (POST "/signup"
                                            {:data (pr-str @가입상태)
                                             :success #()
                                             :failure #()}))}])
        (fg {} [:div (pr-str @가입상태)])]])))


(defn 신청2 []
  "가입 2단계: 메일 확인코드 입력"
  (let [fg (fn [속성 & 입력부]
             [:div.form-group (dissoc 속성 :label)
              [:label.control-label.col-sm-3 (:label 속성)]
              [into [:div.col-sm-9] 입력부]])]
    [:div
     [:form.form-horizontal
      (fg {:label "이메일"} [:div.form-control-static (:이메일 @가입상태)])
      (fg {:label "아이디"} [:div.form-control-static (:아이디 @가입상태)])
      (fg {:label  "확인코드"} [:input.form-control
                                {:name "code" :type "text"
                                 :placeholder "메일에 적힌 6자리 숫자"
                                 :auto-focus true}])
      (fg {} (다음버튼 {:disabled false})
          " "
          [:button.btn.btn-info {:title "확인 메일 재발송 요청하기"}
           "재발송 " [:i.fa.fa-send]])
      (:anti-forgery-field @가입상태)]
     [:div.alert.alert-info.text-center
      "보내드린 메일에 있는 " [:strong "확인코드 "] "6자리 숫자를 입력해주세요. "]]))
