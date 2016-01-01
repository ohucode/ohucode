(ns ohucode.view
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]])
  (:use [ohucode.core]
        [hiccup.core]
        [hiccup.page]))

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
        (if (admin? req)
          [:li [:a {:href "/admin"} "관리자"]])
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

(defprotocol TimeRenderer
  (^String to-human-time [t] "읽기 좋은 시간 표현. ex. 5분전")
  (^String to-exact-time [t] "정확한 일시 표현. ex. 2015-01-01 14:35:03"))

(let [df (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss")]
  (defn- exact-time [ms]
    (.format df (java.util.Date. ms))))

(defn- human-time [ms]
  (let [now   (System/currentTimeMillis)
        dsec  (quot (- now ms) 1000)
        dmin  (quot dsec 60)
        dhour (quot dmin 60)
        dday  (quot dhour 24)]
    (cond
      (< dsec 60) (str dsec "초 전")
      (< dmin 60) (str dmin "분 전")
      (< dhour 24) (str dhour "시간 전")
      (<= dday 15) (str dday "일 전")
      :else (exact-time ms))))

(extend-protocol TimeRenderer
  java.sql.Timestamp
  (to-human-time [ts] (human-time (.getTime ts)))
  (to-exact-time [ts] (exact-time (.getTime ts)))
  java.util.Date
  (to-human-time [d] (human-time (.getTime d)))
  (to-exact-time [d] (exact-time (.getTime d))))

(extend-protocol hiccup.compiler/HtmlRenderer
  java.sql.Timestamp
  (render-html [ts] (to-human-time ts))
  java.util.Date
  (render-html [d] (to-human-time d)))
