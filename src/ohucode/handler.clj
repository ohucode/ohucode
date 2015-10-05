(ns ohucode.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [aleph.http :as http]
            [manifold.stream :as s]
            [ohucode.view :as view]
            [ohucode.git :as git]
            [ohucode.git-http :as git-http])
  (:import [java.util Locale]
           [java.io PipedInputStream PipedOutputStream]))

(defn index [req] "한글 인덱스 뷰! 별도 함수 리로드 확인. 이건 알레프도 되요.")

(println "핸들러 로드")

(defn no-cache [response]
  (-> response
      (header "Cache-Control" "no-cache, max-age=0, must-revalidate")
      (header "Expires" "Fri, 01 Jan 1980 00:00:00 GMT")
      (header "Pragma" "no-cache")))

(defn info-refs-handler [{{svc :service} :params}]
  (if-not (contains? #{"git-receive-pack" "git-upload-pack"} svc)
    {:status 403 :body "not a valid service request"}
    (let [out (PipedOutputStream.)
          in (PipedInputStream. out)]
      (future (git-http/advertise (git/open ".") svc out) (.close out))
      (no-cache
       {:status 200
        :headers {"Content-Type" (str "application/x-" svc "-advertisement")}
        :body in}))))

(defn upload-pack-handler [request]
  (let [repo (git/open ".")
        out (PipedOutputStream.)
        in (PipedInputStream. out)]
    (future (git-http/upload-pack repo (:body request) out) (.close out))
    (no-cache
     {:status 200
      :headers {"Content-Type" "application/x-git-upload-pack-result"}
      :body in})))

(defn receive-pack-handler [request]
  (prn (:body request))
  "let's rock!")

(defroutes app-routes
  (GET "/" [] index)
  (GET "/chunked" []
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (java.io.ByteArrayInputStream. (.getBytes "청크드 test"))})
  (POST "/" [] "post test")
  (context "/:user/:project" [user project]
    (GET "/" [] (str user "/" "project"))
    (GET "/info/refs" [] info-refs-handler)
    (POST "/git-upload-pack" [] upload-pack-handler)
    (POST "/git-receive-pack" [] receive-pack-handler))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (wrap-defaults app-routes api-defaults))

(def app-dev
  (-> app
      (wrap-reload)
      (wrap-stacktrace)
      (wrap-lint)
      (wrap-with-logger)))

;; start-server로 시작한 내용은 리로드되지 않음. 왜?
(defn start []
  (Locale/setDefault Locale/US)
  (http/start-server app-dev {:port 10000}))

(println "handler")
