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
                 [:a {:href "/templates/sign-up-2"} "가입2단계"]]]]]))
   (GET "/templates/sign-up-2" [] (signup/sign-up-wait-confirm))))

(println (str *ns* " reloaded"))
