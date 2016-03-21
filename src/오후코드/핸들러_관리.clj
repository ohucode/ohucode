(ns 오후코드.핸들러-관리
  (:use [미생.기본]
        [오후코드.기본]
        [compojure.core]
        [ring.util.response]
        [hiccup.core])
  (:require [오후코드.db :as db]
            [오후코드.뷰 :as 뷰]))

(함수 admin-nav [req]
  [:ul.nav.nav-tabs
   [:li.active [:a {:href "/admin/users"} "가입자"]]
   [:li [:a {:href "/admin/audits"} "사용자행위"]]
   [:li [:a {:href "/admin/stats"} "통계"]]])

(함수 admin-layout [req title & body]
  (뷰/레이아웃 req {:title (서비스명+ "> 관리자 > " title)}
               [:div.container
                [:div.row (admin-nav req)]
                [:p]
                [:div.row body]]))

(함수- userid-link [userid]
  [:a {:href (str "/admin/users/" userid)} userid])

(함수- timestamp [^java.sql.Timestamp ts]
  [:span {:data-toggle "tooltip" :title (뷰/to-exact-time ts)} ts])

(함수 대시보드 [users]
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

(함수 users [req]
  (가정 [u (db/이용자-목록)]
    (admin-layout req "홈" (대시보드 u))))

(함수 recent-audits [req]
  (가정 [audits (db/기록-검색)]
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

(함수- wrap-admin-only [handler]
  (fn [req]
    (만약 (관리자? req)
      (handler req)
      (뷰/요청에러 req "관리자 권한 필요"))))

(정의 관리-라우트
  (wrap-routes
   (context "/admin" [admin]
     (GET "/" req users)
     (GET "/users" req users)
     (GET "/users/:userid" [userid :as req] 뷰/미구현)
     (GET "/audits" req recent-audits)
     (GET "/audits/:action" [action :as req] 뷰/미구현))
   wrap-admin-only))
