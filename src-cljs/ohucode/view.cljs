(ns ohucode.view
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [cljsjs.marked]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [ohucode.state :refer [app-state signup-state signup-valid-state history]]))

(def 서비스명 "오후코드")

(defn POST [url attrs]
  )

(defn 다음버튼 [속성]
  [:button.btn.btn-primary
   (assoc 속성 :type "submit") "다음 " (if (:waiting 속성) [:i.fa.fa-spin.fa-spinner] [:i.fa.fa-angle-double-right])])

(defn input-control [속성 & 본문]
  (into [:input.form-control 속성] 본문))

(declare 가입-2단계)

(let [form-state (r/atom {:waiting false})]
  (defn signup-form []
    (let [fg (fn [속성 & 입력부]
               [:div.form-group (dissoc 속성 :label)
                [:label.control-label.col-sm-3 (:label 속성)]
                (into [:div.col-sm-9] 입력부)])
          on-change (fn [key]
                      (fn [e] (swap! signup-state assoc key (.-target.value e))))
          validity-class (fn [key]
                           (case (@signup-valid-state key)
                             true "has-success"
                             false "has-error"
                             ""))
          set-userid-by-email (fn [e]
                                (js/console.log "블러 불림" (:userid @signup-state))
                                (if (empty? (:userid @signup-state))
                                  (swap! signup-state assoc :userid
                                         (-> @signup-state :email (.split "@") first))))]
      [:form.form-horizontal
       [:fieldset {:disabled (:waiting @form-state)}
        (fg {:label "이메일" :class (validity-class :email) :disabled true}
            [input-control {:type "email" :name "이메일" :value (:email @signup-state)
                            :auto-focus true
                            :auto-complete "email"
                            :placeholder "이메일 주소"
                            :on-change (on-change :email)
                            :on-blur set-userid-by-email}])
        (fg {:label "아이디" :class (validity-class :userid)}
            [input-control {:type "text" :placeholder "사용할 아이디" :name "아이디"
                            :value (:userid @signup-state)
                            :auto-complete "username"
                            :on-change (on-change :userid)}])
        (fg {:label "비밀번호" :class (validity-class :password)}
            [input-control {:type "password" :placeholder "사용할 비밀번호"
                            :name "비밀번호" :value (:password @signup-state)
                            :auto-complete "current-password"
                            :on-change (on-change :password)}])
        (fg {} [다음버튼 {:disabled (not (:form @signup-valid-state))
                          :waiting (:waiting @form-state)
                          :on-click (fn [e]
                                      (.preventDefault e)
                                      (swap! form-state assoc :waiting true)
                                      (js/setTimeout #(do (swap! form-state dissoc :waiting)
                                                          (swap! app-state assoc :page 가입-2단계))
                                                     2000)
                                      (POST "/signup"
                                            {:data (pr-str @signup-state)
                                             :success #()
                                             :failure #()})
                                      (js/console.log "클릭" @form-state))}])
        (fg {} [:div (pr-str @signup-state)])]])))

(def ^:private signup-step-texts
  ["아이디/이메일 입력"
   "확인 코드 입력"
   "기본 프로필"
   "이용약관 동의"])

(defn- signup-progress []
  [:ul.nav.nav-pills.nav-stacked
   (for [[step text] (map vector (iterate inc 1) signup-step-texts)]
     [:li.disabled {:class (if (= step 1) "active")}
      [:a {:href "#"} text]])])

(defn- 가입-레이아웃 [form & body]
  [:div.container.narrow-container
   [:div.page-header
    [:h2 [:i.fa.fa-user-plus] " 회원가입 "
     [:small (signup-step-texts (:step @signup-state))]]]
   [:div.row
    [:div.col-sm-8
     (into [:div.panel.panel-ohucode
            [:div.panel-body form]]
           body)]
    [:div.col-sm-4 signup-progress]]])

(defn 가입-2단계 []
  "가입 2단계: 메일 확인코드 입력"
  (let [fg (fn [속성 & 입력부]
             [:div.form-group (dissoc 속성 :label)
              [:label.control-label.col-sm-3 (:label 속성)]
              (into [:div.col-sm-9] 입력부)])]
    [:div
     [:form.form-horizontal
      (fg {:label "이메일"} [:div.form-control-static (:email @signup-state)])
      (fg {:label "아이디"} [:div.form-control-static (:userid @signup-state)])
      (fg {:label  "확인코드"} [:input.form-control
                                {:name "code" :type "text"
                                 :placeholder "메일로 보내드린 6자리 숫자"
                                 :auto-focus true}])
      (fg {} (다음버튼 {:disabled false})
          " "
          [:button.btn.btn-info {:title "확인 메일 재발송 요청하기"}
           "재발송 " [:i.fa.fa-send]])
      (:anti-forgery-field @signup-state)]
     [:div.alert.alert-info.text-center
      "보내드린 메일에 있는 " [:strong "확인코드 "] "6자리 숫자를 입력해주세요. "]]))

(defn section [header-title & body]
  (into [:div [:div.page-header>h2 header-title]]
        body))

(defn terms-of-service []
  [:div "서비스 이용약관"])

(defn privacy-policy []
  [:div "개인정보 보호정책"])

(defn markdown [props]
  (let [src (r/atom "<i class='fa fa-spin fa-spinner'></i>")]
    (js/$.ajax #js {:url (:url props)
                    :cache false
                    :success #(reset! src (js/marked % #js {:sanitize true}))})
    (fn [props] [:div {:dangerouslySetInnerHTML #js {:__html @src}}])))

(defn credits []
  (section "고마움을 전합니다"
           [markdown {:url "/md/CREDITS.md"}]))

(defn welcome-guest []
  [:div.jumbotron
   [:div.row
    [:div.col-xs-6.col-md-8
     [:h1 서비스명]
     [:p "즐겁고 효율적인 프로그래밍의 동반자, " 서비스명 "에 오신 것을 환영합니다. "
      서비스명 "는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git 리모트 리포지토리)를 "
      "편리하게 제공합니다."]]
    [:div.col-xs-6.col-md-4
     [:div.panel.panel-signup
      [:div.panel-body
       [:div.page-header [:h4 [:i.fa.fa-user-plus] " 가입 신청"]]
       [signup-form]]]]]])

(defn admin?
  "로그인한 사용자에게 관리자 권한이 있나?"
  [] (= "admin" (get-in @app-state [:user :userid])))

(defn a-link
  "a 태그와 동일하지만, 페이지를 바꾸지 않고 라우팅 처리한다."
  [props & body]
  (let [href (:href props)]
    (into [:a (assoc props :on-click (fn [e]
                                       (.preventDefault e)
                                       (.setToken history href)))]
          body)))

(defn navigation [props]
  [:nav.navbar.navbar-inverse.navbar-static-top
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse"
                                       :data-target "#navbar" :aria-expanded false
                                       :aria-controls "navbar"}
      [:span.sr-only "내비게이션 여닫기"]
      [:span.icon-bar][:span.icon-bar][:span.icon-bar]]
     [a-link {:class "navbar-brand" :href "/"} [:i.fa.fa-git-square] " " 서비스명]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li [a-link {:href "/"} "홈"]]
      [:li [a-link {:href "/help"} "도움말"]]]
     (if-let [사용자 (get-in @app-state [:session :user])]
       [:ul.nav.navbar-nav.navbar-right
        (if (admin?)
          [:li [:a {:href "/admin"} "관리자"]])
        [:li [:a {:href "#" :title "새 저장소"} [:span.octicon.octicon-plus]]]
        [:li [:a {:href "/user/logout"} (:userid 사용자)]]]
       [:ul.nav.navbar-nav.navbar-right
        [:li [:a {:href "/user/login"} [:i.fa.fa-sign-in] " 로그인"]]])]]])

(defn 꼬리말 []
  [:div.container
   [:div.row
    [:ul.list-inline
     [:li "Copyright " [:i.fa.fa-copyright] " 2016 " 서비스명]
     [:li [a-link {:href "/privacy-policy"} "개인정보보호정책"]]
     [:li [a-link {:href "/terms-of-service"} "이용약관"]]
     [:li [a-link {:href "/credits"} "감사의 말"]]]]])

(defn empty-page []
  [:div])

(defn app-page []
  [:div
   [:nav [navigation]]
   [:main [:div.container-fluid [(or (:page @app-state))]]]
   [:footer [꼬리말]]])
