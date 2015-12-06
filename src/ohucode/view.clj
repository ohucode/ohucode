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
     (include-js "//cdn.jsdelivr.net/vue.validator/2.0.0-alpha.5/vue-validator.min.js")
     (include-js "/js/vue-router.min.js")
     (include-js "/js/ohucode.js")]]))

(defn anti-forgery-field []
  [:input {:type "hidden" :name "__anti-forgery-token"
           :value *anti-forgery-token*}])
