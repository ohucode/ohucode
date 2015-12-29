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
               [:div.col-sm-12
                [:div.page-header [:h2 "템플릿"]]
                [:ul.list-group
                 (for [[path text] [["/templates/signup-1" "가입1단계"]
                                    ["/templates/signup-2" "가입2단계"]
                                    ["/templates/signup-3" "가입3단계"]
                                    ["/templates/signup-4" "가입4단계"]]]
                   [:li.list-group-item
                    [:a {:href path} text]])]]]]))
   (GET "/templates/signup-1" req signup/signup-step1)
   (GET "/templates/signup-2" req (signup/signup-step2 req "hatemogi@gmail.com" "hatemogi"))
   (GET "/templates/signup-3" req (signup/signup-step3 req "hatemogi@gmail.com" "hatemogi" "123456"))
   (GET "/templates/signup-4" req (signup/signup-step4 req "hatemogi@gmail.com" "hatemogi"))))
