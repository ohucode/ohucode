(ns ohucode.view
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]])
  (:use [misaeng.core]
        [ohucode.core]
        [hiccup.core]
        [hiccup.page]))

(함수 navigation [요청]
  [:nav.navbar.navbar-inverse.navbar-static-top
   [:div.container-fluid
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse"
                                       :data-target "#navbar" :aria-expanded false
                                       :aria-controls "navbar"}
      [:span.sr-only "내비게이션 여닫기"]
      (반복 3 [:span.icon-bar])]
     [:a.navbar-brand {:href "/"} [:i.fa.fa-git-square] " " 서비스명]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li {:class "active"}
       "<a v-link=\"{ path: '/' }\">홈</a>"]
      [:li
       "<a v-link=\"{ path: '/about' }\">소개</a>"]
      [:li
       "<a v-link=\"{ path: '/help' }\">도움말</a>"]]
     (만약-가정 [사용자 (get-in 요청 [:session :user])]
       [:ul.nav.navbar-nav.navbar-right
        (만약 (관리자? 요청)
          [:li [:a {:href "/admin"} "관리자"]])
        [:li [:a {:href "#" :title "새 저장소"} [:span.octicon.octicon-plus]]]
        [:li [:a {:href "/user/logout"} (:userid 사용자)]]]
       [:ul.nav.navbar-nav.navbar-right
        [:li [:a {:href "/user/login"} [:i.fa.fa-sign-in] " 로그인"]]])]]])

(함수 꼬리말 [요청]
  [:footer
   [:div.container [:div.row [:ul.list-inline
                              [:li "Copyright " [:i.fa.fa-copyright] " 2016 " 서비스명]
                              [:li [:a {:href "/privacy-policy"} "개인정보보호정책"]]
                              [:li [:a {:href "/terms-of-service"} "이용약관"]]
                              [:li [:a {:href "/credits"} "감사의 말"]]]]]])

(함수 레이아웃 [요청 옵션 & 본문]
  "opts {:title "" :css [] :js []}"
  {:pre (seq? (:js 옵션))}
  (html5 {:lang "ko"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
          [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
          [:title (get 옵션 :title 서비스명)]
          (맵 include-css
              (리스트* "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
                       "//maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css"
                       "/css/octicons/octicons.css"
                       "/css/ohucode.css"
                       (:css 옵션)))]
         [:body#app
          (navigation 요청)
          (만약-가정 [flash (:flash 요청)]
            flash)
          [:div.container-fluid.main-wrap
           [:main 본문]]
          (꼬리말 요청)
          (맵 include-js
              (리스트* "//ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"
                       "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"
                       "/js/vue.min.js"
                       "/js/marked.min.js"
                       "/js/ohucode.js"
                       (:js 요청)))]))

(함수 anti-forgery-field []
  [:input {:type "hidden" :name "__anti-forgery-token"
           :value *anti-forgery-token*}])

(프로토콜 TimeRenderer
  (^String to-human-time [t] "읽기 좋은 시간 표현. ex. 5분전")
  (^String to-exact-time [t] "정확한 일시 표현. ex. 2015-01-01 14:35:03"))

(가정 [df (java.text.SimpleDateFormat. "yyyy/MM/dd HH:mm:ss")]
  (함수- exact-time [ms]
    (.format df (java.util.Date. ms))))

(함수- human-time [ms]
  (가정 [now   (System/currentTimeMillis)
        dsec  (몫 (- now ms) 1000)
        dmin  (몫 dsec 60)
        dhour (몫 dmin 60)
        dday  (몫 dhour 24)]
    (조건
      (< dsec 60) (str dsec "초 전")
      (< dmin 60) (str dmin "분 전")
      (< dhour 24) (str dhour "시간 전")
      (<= dday 15) (str dday "일 전")
      :else (exact-time ms))))

(프로토콜-확장 TimeRenderer
  java.sql.Timestamp
  (to-human-time [ts] (human-time (.getTime ts)))
  (to-exact-time [ts] (exact-time (.getTime ts)))
  java.util.Date
  (to-human-time [d] (human-time (.getTime d)))
  (to-exact-time [d] (exact-time (.getTime d))))

(프로토콜-확장 hiccup.compiler/HtmlRenderer
  java.sql.Timestamp
  (render-html [ts] (to-human-time ts))
  java.util.Date
  (render-html [d] (to-human-time d)))
