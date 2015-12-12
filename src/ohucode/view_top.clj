(ns ohucode.view-top
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]
        [ohucode.view-signup]))

(defn not-found [req]
  (layout {:title (brand-name+ "> 찾을 수 없습니다")}
          [:div.container
           [:div.row
            [:h1 "찾을 수 없습니다."]
            [:p "요청하신 페이지를 찾을 수 없습니다."]]]))

(defn terms-of-service [_]
  (layout {:title (brand-name+ "> 서비스 이용약관")}
          [:div.container
           [:div.row
            [:h1 "서비스 이용약관"]
            [:p "오후코드 서비스를 이용하면..."]]]))

(defn privacy-policy [_]
  (layout {:title (brand-name+ "> 개인정보 보호정책")}
          [:div.container
           [:div.row
            [:h1 "개인정보 보호정책"]
            [:p "오후코드 서비스를 이용하면..."]]]))

(defn intro-guest [_]
  (layout
   {:title (brand-name+ "첫화면")}
   [:div.jumbotron
    [:div.row
     [:div.col-sm-6.col-md-8
      [:h1 brand-name]
      [:p "즐겁고 효율적인 프로그래밍의 동반자, " brand-name "에 오신 것을 환영합니다. "
       brand-name "는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git 리모트 리포지토리)를 편리하게 제공합니다."]]
     [:div.col-sm-6.col-md-4
      [:div.panel.panel-default
       [:div.panel-body
        [:div.page-header [:h4 "가입 신청"]]
        (signup-form _)]]]]]
   [:div.container
    [:div.row
     [:div.page-header [:h1 "Git 저장소 서비스"]]
     [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
     [:div.page-header [:h1 "위키 페이지 작성"]]]]
))
