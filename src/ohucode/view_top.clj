(ns ohucode.view-top
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.view]
        [ohucode.view-signup]))

(defn terms-of-service [_]
  (layout {:title "오후코드 > 서비스 이용약관"}
          [:div.container-fluid
           [:div.row
            [:h1 "서비스 이용약관"]
            [:p "오후코드 서비스를 이용하면..."]]]))

(defn privacy-policy [_]
  (layout {:title "오후코드 > 개인정보 보호정책"}
          [:div.container-fluid
           [:div.row
            [:h1 "개인정보 보호정책"]
            [:p "오후코드 서비스를 이용하면..."]]]))

(defn intro-guest [_]
  (layout
   {:title "오후코드 첫화면"}
   [:div.jumbotron
    [:div.row
     [:div.col-sm-8.col-xs-12
      [:h1 "오후코드"]
      [:p "즐겁고 효율적인 프로그래밍의 동반자, 오후코드에 오신 것을 환영합니다."
       "오후코드는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git) 저장소를 편리하게 제공합니다."]]
     [:div.col-sm-4.col-xs-6
      (sign-up-form _)]]]))

(println (str *ns* " reloaded"))
