(ns ohucode.view
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]])
  (:use [hiccup.core]
        [hiccup.page]))

(def brand-name "오후코드")

(defn brand-name+ [& strs]
  (apply str (concat brand-name " " strs)))

(defn navigation [req]
  [:nav.navbar.navbar-inverse.navbar-static-top
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse"
                                       :data-target "#navbar" :aria-expanded false
                                       :aria-controls "navbar"}
      [:span.sr-only "내비게이션 여닫기"]
      (repeat 3 [:span.icon-bar])]
     [:a.navbar-brand {:href "/"} [:i.fa.fa-git-square] " " brand-name]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li {:class "active"}
       "<a v-link=\"{ path: '/' }\">홈</a>"]
      [:li
       "<a v-link=\"{ path: '/about' }\">소개</a>"]
      [:li
       "<a v-link=\"{ path: '/help' }\">도움말</a>"]]
     (if-let [user (get-in req [:session :user])]
       [:ul.nav.navbar-nav.navbar-right
        [:li [:a {:href "/user/logout"} (:userid user)]]]
       [:ul.nav.navbar-nav.navbar-right
        [:li [:a {:href "/user/login"} [:i.fa.fa-sign-in] " 로그인"]]])]]])

(defn footer [req]
  [:footer
   [:div.container [:div.row [:ul.list-inline
                              [:li "Copyright " [:i.fa.fa-copyright] " 2015 " brand-name]
                              [:li [:a {:href "/privacy-policy"} "개인정보보호정책"]]
                              [:li [:a {:href "/terms-of-service"} "이용약관"]]]]]])

(defn layout [req opts & body]
  "opts {:title "" :css [] :js []}"
  {:pre (seq? (:js opts))}
  (html5 {:lang "ko"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
          [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
          [:title (get opts :title brand-name)]
          (map include-css
               (list* "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
                      "//maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css"
                      "/css/ohucode.css"
                      (:css opts)))]
         [:body#app
          (navigation req)
          (if-let [flash (:flash req)]
            flash)
          [:div.container-fluid.main-wrap
           [:main body]]
          (footer req)
          (map include-js
               (list* "//ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"
                      "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"
                      "/js/vue.min.js"
                      "/js/ohucode.js"
                      (:js opts)))]))

(defn anti-forgery-field []
  [:input {:type "hidden" :name "__anti-forgery-token"
           :value *anti-forgery-token*}])
