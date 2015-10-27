(ns ohucode.admin-handler
  (:require [compojure.core :refer :all]
            [ring.util.response :refer :all]
            [ohucode.db :as db]
            [net.cgrand.enlive-html :as h]
            [net.cgrand.reload :as reload]
            [ohucode.view :as v]))

(h/defsnippet dashboard "templates/admin/dashboard.html"
  [:main]  
  [users]
  [:#user-list :.user-entry]
  (h/clone-for [u users]
               [:.id]       (h/content (:id u))
               [:.name]     (h/content (:name u))
               [:.location] (h/content (:location u))
               [:.date]     (h/content (str (:created_at u)))))

;; TODO: make reloading affect only in dev mode
(reload/auto-reload *ns*)

(defn users [req]
  "welcome to user list")

(def admin-routes
  ;; TODO: wrap a handler that authorizes the req is from an admin
  (context "/admin" [admin]
    (GET "/" [] (let [u (db/select-users)]
                  (prn u)
                  (v/layout {:title "오후코드 관리자"
                             :main (dashboard u)})))
    (GET "/users" [] users)))
