(ns ohucode.view
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]])
  (:use [hiccup.core]
        [hiccup.page]))

(defn navigation []
  [:nav.navbar.navbar-inverse.navbar-fixed-top
   [:div.container-fluid
    [:div.navbar-header
     [:button {:type "button" :class "navbar-toggle collapsed" :data-toggle "collapse" :data-target "#navbar" :aria-expanded "false" :aria-controls "navbar"}
      [:span.sr-only "Toggle navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a.navbar-brand {:href "/"} "오후코드"]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li {:class "active"}
       "<a v-link=\"{ path: '/' }\">홈</a>"]
      [:li
       "<a v-link=\"{ path: '/about' }\">소개</a>"]
      [:li
       "<a v-link=\"{ path: '/help' }\">도움말</a>"]]
     [:ul.nav.navbar-nav.navbar-right
      [:li
       [:a {:href "/"} "로그인"]]]]]])

(defn footer []
  [:footer [:ul.list-inline
            [:li "Copyright 2015 오후코드"]
            [:li
             [:a {:href "/privacy-policy"} "개인정보보호정책"]]
            [:li
             [:a {:href "/terms-of-service"} "이용약관"]]]])

(defn layout [opts & body]
  (html5
   [:html {:lang "ko"}
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
     [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
     [:title (get opts :title "오후코드 템플릿")]
     (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css")
     (include-css "/css/ohucode.css")]
    [:body#app
     (navigation)
     [:div.container-fluid.main-wrap
      [:main body]
      (footer)]
     (include-js "//ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js")
     (include-js "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js")
     (include-js "/js/vue.min.js")
     (include-js "/js/vue-validator.min.js")
     (include-js "/js/vue-router.min.js")
     (include-js "/js/ohucode.js")]]))

(defn anti-forgery-field []
  [:input {:type "hidden" :name "__anti-forgery-token"
           :value *anti-forgery-token*}])

(defn intro-guest []
  (layout
   {:title "오후코드 첫화면"}
   [:div.jumbotron
    [:div.row
     [:div.col-sm-8.col-xs-12
      [:h1 "오후코드"]
      [:p "즐겁고 효율적인 프로그래밍의 동반자, 오후코드에 오신 것을 환영합니다."
       "오후코드는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git) 저장소를 편리하게 제공합니다."]]
     [:div.col-sm-4.col-xs-6
      [:form#sign-up-form {:method "POST", :action "/sign-up"}
       [:div.form-group
        [:label {:for "sign-up-email"} "이메일"]
        [:input#sign-up-email.form-control {:name "email" :type "email" :placeholder "username@yourmail.net" :autofocus true :required true}]]
       [:div.form-group
        [:label {:for "sign-up-password"} "패스워드"]
        [:input#sign-up-password.form-control {:name "password" :type "password" :placeholder "영문숫자혼합패스워드" :required true }]]
       (anti-forgery-field)
       [:button.btn.btn-lg.btn-primary {:type "submit"} "가입하기"]
       ]]]]))

(defn sign-up-wait-confirm []
  (layout {:title "오후코드 가입 > 2단계"}
          [:div.row
           [:div.col-sm-3
            [:ul.list-group
             [:li.list-group-item "1. 가입"]
             [:li.list-group-item "2. 이메일 주소 확인"]]]
           [:div.col-sm-9
            [:ol.breadcrumb
             [:li "가입"]
             [:li "이메일 입력"]
             [:li "이메일 확인"]]]]))

(defn terms-of-service [_]
  (layout {:title "오후코드 > 서비스 이용약관"}
          [:div.container
           [:div.row
            [:h1 "서비스 이용약관"]
            [:p "오후코드 서비스를 이용하면..."]]]))

(defn privacy-policy [_]
  (layout {:title "오후코드 > 개인정보 보호정책"}
          [:div.container
           [:div.row
            [:h1 "개인정보 보호정책"]
            [:p "오후코드 서비스를 이용하면..."]]]))

(println (str *ns* " reloaded"))
