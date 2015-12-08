(ns ohucode.handler-templates
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ohucode.view-signup :as signup])
  (:use [ohucode.view]))

(def template-routes
  (routes
   (GET "/templates" []
     (layout {:title "템플릿 확인"}
             [:div.container
              [:div.row
               [:ul.list-group
                [:li.list-group-item
                 [:a {:href "/templates/signup-1"} "가입1단계: 이메일 입력"]]
                [:li.list-group-item
                 [:a {:href "/templates/signup-2"} "가입2단계"]]]]]))
   (GET "/templates/signup-1" [] signup/signup-step1)
   (GET "/templates/signup-2" [] signup/signup-step2)))

(println (str *ns* " reloaded"))
