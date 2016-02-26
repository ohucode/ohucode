(ns 오후코드.뷰
  (:require [ring.middleware.anti-forgery :refer [*anti-forgery-token*]])
  (:use [미생.기본]
        [오후코드.기본]
        [hiccup.core]
        [hiccup.page]))

;; 제거 예정 (대부분의 뷰는 클로저스크립트로 넘어갑니다)
(함수 레이아웃 [요청 옵션 & 본문]
  "opts {:title "" :css [] :js []}"
  {:pre (seq? (:js 옵션))}
  (html5 {:lang "ko"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
          [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
          [:title (get 옵션 :title 서비스명)]
          (사상 include-css
                (리스트* "//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
                         "//maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css"
                         "/css/color-brewer.css"
                         "/css/octicons/octicons.css"
                         "/css/ohucode.css"
                         (:css 옵션)))]
         [:body
          [:div#app]
          (사상 include-js
                (리스트* "/js/main.js" (:js 요청)))]))

(함수 기본 []
  (html5 {:lang "ko"}
         [:head
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
          [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
          [:title 서비스명]
          (사상 include-css
                ["//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
                 "//maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css"
                 "/css/color-brewer.css"
                 "/css/octicons/octicons.css"
                 "/css/ohucode.css"])]
         [:body
          [:div#app]
          (사상 include-js ["/js/main.js"])]))

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
