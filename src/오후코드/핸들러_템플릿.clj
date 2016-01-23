(ns 오후코드.핸들러-템플릿
  (:require [오후코드.뷰-가입 :as 가입뷰]
            [오후코드.뷰-최상 :as 최상뷰])
  (:use [미생.기본]
        [오후코드.뷰]
        [compojure.core]))

(정의 템플릿-라우트
  (routes
   (GET "/templates" []
     (레이아웃 {} {:title "템플릿 확인"}
             [:div.container
              [:div.row
               [:div.col-sm-12
                [:div.page-header [:h2 "템플릿"]]
                [:ul.list-group
                 (for [[path text] [["/templates/signup-1" "가입1단계"]
                                    ["/templates/signup-2" "가입2단계"]
                                    ["/templates/signup-3" "가입3단계"]
                                    ["/templates/signup-4" "가입4단계"]
                                    ["/templates/login" "로그인"]]]
                   [:li.list-group-item
                    [:a {:href path} text]])]]]]))
   (GET "/templates/signup-1" 요청 가입뷰/가입-1단계)
   (GET "/templates/signup-2" 요청 (가입뷰/가입-2단계 요청 "hatemogi@gmail.com" "hatemogi"))
   (GET "/templates/signup-3" 요청 (가입뷰/가입-3단계 요청 "hatemogi@gmail.com" "hatemogi" "123456"))
   (GET "/templates/signup-4" 요청 (가입뷰/가입-4단계 요청 "hatemogi@gmail.com" "hatemogi"))
   (GET "/templates/login" 요청 최상뷰/로그인-페이지)))
