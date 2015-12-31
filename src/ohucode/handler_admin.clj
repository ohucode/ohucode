(ns ohucode.handler-admin
  (:require [compojure.core :refer :all]
            [ring.util.response :refer :all]
            [ohucode.db :as db]
            [hiccup.core :refer :all]
            [ohucode.core :refer :all]
            [ohucode.view :as v]
            [ohucode.view-top :as v-top]))

(defn dashboard [users]
  [:main
   [:h2 "사용자 리스트"]
   [:div.container
    [:div.row
     [:table#user-list.table
      [:tbody
       [:tr
        [:th "아이디"]
        [:th "이메일"]
        [:th "이름"]
        [:th "지역"]
        [:th "가입일"]]
       (for [u users] [:tr.user-entry
                       [:td.userid (:userid u)]
                       [:td.email (:email u)]
                       [:td.name (:name u)]
                       [:td.location (:location u)]
                       [:td.date (str (:created_at u))]])]]]]])


(defn wrap-admin-only [handler]
  (fn [req]
    (if (= "admin" (:userid (session-user req)))
      (handler req)
      (v-top/request-error req "관리자 권한 필요"))))

(defn users [req]
  "welcome to user list")

(def admin-routes
  (wrap-routes
   (context "/admin" [admin]
     (GET "/" [req] (let [u (db/select-users)]
                      (v/layout req {:title "오후코드 관리자"}
                                (dashboard u))))
     (GET "/users" [] users))
   wrap-admin-only))
