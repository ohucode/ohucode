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
           [java.io PipedInputStream PipedOutputStream]
           [java.util.zip GZIPInputStream GZIPOutputStream]))

(defn index [req] "한글 인덱스 뷰! 별도 함수 리로드 확인. 이건 알레프도 되요.")

(println "핸들러 로드")

(def repo (git/open "fixture/git.git"))
;(def repo (git/open "p"))

(defn- no-cache [response]
  (-> response
      (header "Cache-Control" "no-cache, max-age=0, must-revalidate")
      (header "Expires" "Fri, 01 Jan 1980 00:00:00 GMT")
      (header "Pragma" "no-cache")))

(defn- gzip-response-header [response request]
  (if-not (= "gzip" (:headers "Accept-Encoding"))
    response
    (header response "ContentEncoding" "gzip")))

(defn- gzip-input-stream [request]
  (let [in (:body request)
        gzip #{"gzip" "x-gzip"}
        enc (get-in request [:headers "Content-Encoding"])]
    (if (contains? gzip enc)
      (GZIPInputStream. in)
      in)))

(defn- gzip-output-stream [request out]
  (if (= "gzip" (:headers "Accept-Encoding"))
    (GZIPOutputStream. out)
    out))

(defn info-refs-handler [{{svc :service} :params :as request}]
  (if-not (contains? #{"git-receive-pack" "git-upload-pack"} svc)
    {:status 403 :body "not a valid service request"}
    (let [out (PipedOutputStream.)
          in (PipedInputStream. out)
          out (gzip-output-stream request out)]
      (future (git-http/advertise repo svc out) (.close out))
      (-> {:status 200
           :headers {"Content-Type" (str "application/x-" svc "-advertisement")}
           :body in}
          (no-cache)
          (gzip-response-header request)))))

(defn upload-pack-handler [request]
  (let [out (PipedOutputStream.)
        in (PipedInputStream. out)
        out (gzip-output-stream request out)]
    (future
      (try
        (git-http/upload-pack repo (gzip-input-stream request) out)
        (println "업로드팩 끝")
        (catch Exception e (prn e))
        (finally (.close out))))
    (-> {:status 200
         :headers {"Content-Type" "application/x-git-upload-pack-result"}
         :body in}
        (no-cache)
        (gzip-response-header request))))

(defn receive-pack-handler [request]
  (let [repo repo
        out (PipedOutputStream.)
        in (PipedInputStream. out)
        out (gzip-output-stream request out)]
    (future (git-http/receive-pack repo (gzip-input-stream request) out) (.close out))
    (-> {:status 200
         :headers {"Content-Type" "application/x-git-receive-pack-result"}
         :body in}
        no-cache
        (gzip-response-header request))))

(defn stream-test [request]
  (let [s (s/stream)]
    {:body (let [sent (atom 0)]
             (->> (s/periodically 100 #(str (swap! sent inc) "\n"))
                  (s/transform (take 100))))}))

(defroutes app-routes
  (GET "/" [] index)
  (GET "/stream" [] stream-test)
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
      (wrap-with-logger)))

;; start-server로 시작한 내용은 리로드되지 않음. 왜?
(defn start []
  (Locale/setDefault Locale/US)
  (http/start-server app-dev {:port 10000}))
