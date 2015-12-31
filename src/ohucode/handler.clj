(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [ohucode.core :refer :all]
            [ohucode.db :as db]
            [ohucode.view-top :as v-top]
            [ohucode.handler-git :refer [smart-http-routes]]
            [ohucode.handler-admin :refer [admin-routes]]
            [ohucode.handler-signup :refer [signup-routes]]
            [ohucode.handler-templates :refer [template-routes]]))

(defn- not-implemented [req]
  (throw (UnsupportedOperationException.)))

(defn session-user [req]
  (get-in req [:session :user]))

(def signed-in? (comp not nil? session-user))

(defn wrap-user-info [handler]
  (fn [req]
    (binding [*signed-user* (session-user req)]
      (handler req))))

(defn wrap-signed-user-only [handler]
  (fn [req]
    (if-let [signed-in? req]
      (handler req)
      (v-top/request-error "로그인이 필요합니다."))))

(defn- login [res userid]
  (assoc-in res [:session :user]
            (-> (db/select-user userid)
                (dissoc :password_digest :created_at :updated_at))))

(def user-routes
  (routes
   (context "/user" []
     (GET "/login" req v-top/login-page)
     (POST "/login" [userid password]
       (if (db/valid-user-password? userid password)
         (-> (redirect "/")
             (assoc :flash "로그인 성공")
             (login userid))
         (-> (redirect "/user/login")
             (assoc :flash "인증 실패"))))
     (GET "/logout" req
       (db/insert-audit (or (:userid (session-user req))
                            "guest")
                        "logout" {})
       (-> (v-top/basic-content req "로그아웃" "로그아웃처리")
           response
           (update :session dissoc :user))))
   (context "/:user" [user]
     (GET "/" [] v-top/not-found)
     (GET "/settings" [] not-implemented)
     (GET "/profile" [] not-implemented))))

(def project-routes
  (context "/:user/:project" [user project]
    (GET "/" [] v-top/not-found)
    (GET "/commits" [] not-implemented)
    (GET "/commits/:ref" [ref] not-implemented)
    (GET "/commit/:commit-id" not-implemented-yet)
    (GET "/settings" [] not-implemented)
    (GET "/tree/:ref/:path" [ref path] not-implemented)
    (GET "/blob/:ref/:path" [ref path] not-implemented)
    (GET "/tags" [] not-implemented)
    (GET "/branches" [] not-implemented)
    (GET "/issues" [] not-implemented)))

(def web-routes
  (routes
   (GET "/" req
     (if (signed-in? req)
       v-top/dashboard
       v-top/intro-guest))
   (GET "/throw" [] (throw (RuntimeException. "스택트레이스 실험")))
   (GET "/terms-of-service" [] v-top/terms-of-service)
   (GET "/privacy-policy" [] v-top/privacy-policy)
   signup-routes
   admin-routes
   user-routes
   project-routes))

(defn- wrap-html-content-type [handler]
  (fn [req]
    (if-let [res (handler req)]
      (if (find-header res "Content-Type")
        res
        (content-type res "text/html; charset=utf-8")))))

(defn wrap-bind-client-ip [handler]
  (fn [req]
    (binding [*client-ip* (:remote-addr req)]
      (handler req))))

(defonce ^:private
  ^{:doc "리로드 해도 세션을 유지하기 위해 메모리 세션 따로 둡니다"}
  session-store (memory-store))

(def app
  (routes
   (route/resources "/js" {:root "public/js"})
   (route/resources "/css" {:root "public/css"})

   (wrap-routes smart-http-routes
                wrap-defaults api-defaults)

   (-> web-routes
       wrap-user-info
       wrap-bind-client-ip
       wrap-html-content-type
       (wrap-defaults (-> site-defaults
                          ;; static 자원은 앞에서 미리 처리합니다
                          (dissoc :static)
                          (assoc-in [:session :store] session-store))))

   (ANY "*" [] v-top/not-found)))

(def app-dev
  (-> (routes template-routes app)
      (wrap-exceptions)
      (wrap-reload)))
