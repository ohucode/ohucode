(ns ohucode.top
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [cljsjs.marked]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [ohucode.state :refer [앱상태]]
            [ohucode.signup :as 가입]
            [ohucode.core :refer [서비스명 문단 마크다운 링크 사용자 관리자?]]))

(defn 서비스이용약관 []
  [:div.container-fluid
   [문단 "서비스이용약관"
    [:div "오후코드 서비스를 이용하시면, 아래와..."]]])

(defn 개인정보보호정책 []
  [:div.container-fluid
   [문단 "개인정보보호정책"
    [:div "개인정보를 중요하게 생각합니다."]]])

(defn 감사의말 []
  (문단 "고마움을 전합니다"
        [마크다운 {:url "/md/CREDITS.md"}]))

(defn 손님첫페이지 []
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
       [가입/신청1]]]]]])

(defn 네비게이션 [속성]
  [:nav.navbar.navbar-inverse.navbar-static-top
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse"
                                       :data-target "#navbar" :aria-expanded false
                                       :aria-controls "navbar"}
      [:span.sr-only "내비게이션 여닫기"]
      [:span.icon-bar][:span.icon-bar][:span.icon-bar]]
     [링크 {:class "navbar-brand" :href "/"} [:i.fa.fa-git-square] " " 서비스명]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li [링크 {:href "/"} "홈"]]
      [:li [링크 {:href "/help"} "도움말"]]]
     (if-let [사용자 (사용자)]
       [:ul.nav.navbar-nav.navbar-right
        (if (관리자?)
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
     [:li [링크 {:href "/privacy-policy"} "개인정보보호정책"]]
     [:li [링크 {:href "/terms-of-service"} "이용약관"]]
     [:li [링크 {:href "/credits"} "감사의 말"]]]]])

(defn 앱페이지 []
  [:div
   [:nav [네비게이션]]
   [:main [:div.container-fluid
           [@앱상태 :page]]]
   [:footer [꼬리말]]])
