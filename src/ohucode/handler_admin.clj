(ns ohucode.handler-admin
  (:require [compojure.core :refer :all]
            [ring.util.response :refer :all]
            [ohucode.db :as db]
            [hiccup.core :refer :all]
            [ohucode.view :as v]))

(defn dashboard [users]
  [:main
   [:h2 "사용자 리스트"]
   [:div.container
    [:div.row
     [:table#user-list.table
      [:tbody
       [:tr
        [:th "아이디"]
        [:th "이름"]
        [:th "지역"]
        [:th "가입일"]]
       (for [u users] [:tr.user-entry
                       [:td.id (:id u)]
                       [:td.name (:name u)]
                       [:td.location (:location u)]
                       [:td.date (str (:created_at u))]])]]]]])


(defn users [req]
  "welcome to user list")

(def admin-routes
  ;; TODO: wrap a handler that authorizes the req is from an admin
  (context "/admin" [admin]
    (GET "/" [] (let [u (db/select-users)]
                  (prn u)
                  (v/layout {:title "오후코드 관리자"}
                            (dashboard u))))
    (GET "/users" [] users)))

(println (str *ns* " reloaded"))
