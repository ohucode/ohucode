(ns ohucode.handler-admin
  (:require [compojure.core :refer :all]
            [ring.util.response :refer :all]
            [ohucode.db :as db]
            [hiccup.core :refer :all]
            [ohucode.core :refer :all]
            [ohucode.view :as v]
            [ohucode.view-top :as v-top]))

(defn admin-nav [req]
  [:ul.nav.nav-tabs
   [:li.active [:a {:href "/admin/users"} "가입자"]]
   [:li [:a {:href "/admin/audits"} "사용자행위"]]
   [:li [:a {:href "/admin/stats"} "통계"]]])

(defn admin-layout [req title & body]
  (v/layout req {:title (brand-name+ "> 관리자 > " title)}
          [:div.container
           [:div.row (admin-nav req)]
           [:p]
           [:div.row body]]))

(defn- userid-link [userid]
  [:a {:href (str "/admin/users/" userid)} userid])

(defn- timestamp [^java.sql.Timestamp ts]
  [:span {:data-toggle "tooltip" :title (v/to-exact-time ts)} ts])

(defn dashboard [users]
  [:main
   [:h2 "사용자 리스트"]
   [:div.container
    [:div.row
     [:table#user-list.table.table-striped
      [:tbody
       [:tr
        [:th "아이디"]
        [:th "이메일"]
        [:th "이름"]
        [:th "지역"]
        [:th "가입일"]]
       (for [u users] [:tr.user-entry
                       [:td.userid (userid-link (:userid u))]
                       [:td.email (:email u)]
                       [:td.name (:name u)]
                       [:td.location (:location u)]
                       [:td.date (timestamp (:created_at u))]])]]]]])

(defn users [req]
  (let [u (db/select-users)]
    (admin-layout req "홈" (dashboard u))))

(defn recent-audits [req]
  (let [audits (db/select-audits)]
    (admin-layout req "행위"
                  [:table#audit-list.table.table-striped
                   [:tbody
                    [:tr
                     [:th "아이디"]
                     [:th "action"]
                     [:th "data"]
                     [:th "IP"]
                     [:th "일시"]]
                    (for [a audits] [:tr.audit-entry
                                     [:td.userid (userid-link (:userid a))]
                                     [:td.action (:action a)]
                                     [:td.data (:data a)]
                                     [:td.ip (:ip a)]
                                     [:td.date (timestamp (:created_at a))]])]])))

(defn- wrap-admin-only [handler]
  (fn [req]
    (if (= "admin" (:userid (session-user req)))
      (handler req)
      (v-top/request-error req "관리자 권한 필요"))))

(def admin-routes
  (wrap-routes
   (context "/admin" [admin]
     (GET "/" req users)
     (GET "/users" req users)
     (GET "/users/:userid" [userid :as req] v-top/not-implemented)
     (GET "/audits" req recent-audits)
     (GET "/audits/:action" [action :as req] v-top/not-implemented))
   wrap-admin-only))
