(ns 오후코드.핸들러-깃
  (:use [미생.기본]
        [compojure.core]
        [ring.util.response])
  (:require [오후코드.git :as git])
  (:import [java.io InputStream OutputStream ByteArrayOutputStream
            PipedInputStream PipedOutputStream]
           [java.util.zip GZIPInputStream GZIPOutputStream]
           [org.eclipse.jgit.transport
            UserAgent PacketLineOut
            RefAdvertiser$PacketLineOutRefAdvertiser
            ReceivePack PreReceiveHook PostReceiveHook
            UploadPack RefFilter PreUploadHook PostUploadHook]))

(defn advertise [repo svc ^OutputStream out]
  (let [plo (PacketLineOut. out)
        pck (RefAdvertiser$PacketLineOutRefAdvertiser. plo)
        up (cond
             (= svc "git-upload-pack") (UploadPack. repo)
             (= svc "git-receive-pack") (ReceivePack. repo)
             :else (throw (IllegalArgumentException.)))]
    (try
      (.writeString plo (str "# service=" svc "\n"))
      (.end plo)
      (.sendAdvertisedRefs up pck)
      (finally (.. up getRevWalk close)))))

(defn upload-pack [repo ^InputStream in ^OutputStream out]
  (let [ref-filter (reify RefFilter
                     (filter [this refs]
                       (prn (str "filtering for" refs))
                       refs))
        pre-hook (reify PreUploadHook
                   (onBeginNegotiateRound [this up wants cnt-offered] nil)
                   (onEndNegotiateRound [this up wants cnt-common cnt-not-found ready?] nil)
                   (onSendPack [this up wants haves]
                     (prn "pre-hook")
                     (prn up)))
        up (doto (UploadPack. repo)
             (.setBiDirectionalPipe false)
             (.setRefFilter ref-filter)
             (.setPreUploadHook pre-hook))]
    (.upload up in out nil)))

(defn receive-pack [repo ^InputStream in ^OutputStream out]
  (let [rp (doto (ReceivePack. repo)
             (.setBiDirectionalPipe false))]
    (.receive rp in out nil)))

(UserAgent/set "OhuGit/0.0.1")

(def repo (git/open "fixture/fixture-repo/.git"))
;;(def repo (git/open "p"))

(defn- no-cache [response]
  (update response :headers merge
          {"Cache-Control" "no-cache, max-age=0, must-revalidate"
           "Expires" "Fri, 01 Jan 1980 00:00:00 GMT"
           "Pragma" "no-cache"}))

(defn- gzip-input-stream [request]
  (let [in (:body request)
        gzip #{"gzip" "x-gzip"}
        enc (get-in request [:headers "Content-Encoding"])]
    (if (contains? gzip enc)
      (GZIPInputStream. in)
      in)))

(defn- gzip-output-stream [request out]
  (if (= "gzip" (get-in request [:headers "Accept-Encoding"]))
    (GZIPOutputStream. out)
    out))

(defn- gzip-response-header [response request]
  (if (= "gzip" (get-in request [:headers "Accept-Encoding"]))
    (header response "Content-Encoding" "gzip")
    response))

(def ^:private no-cache-and-gzip-response
  (comp no-cache gzip-response-header))

(defn wrap-no-cache-and-gzip [handler]
  (fn [request]
    (if-let [response (handler request)]
      (no-cache-and-gzip-response response request))))

(defn info-refs-handler [{{svc :service} :params :as request}]
  (if-not (contains? #{"git-receive-pack" "git-upload-pack"} svc)
    {:status 403 :body "not a valid service request"}
    (let [out (PipedOutputStream.)
          in (PipedInputStream. out)
          out (gzip-output-stream request out)]
      (future (advertise repo svc out) (.close out))
      (-> (response in)
          (content-type (str "application/x-" svc "-advertisement"))))))

(defn upload-pack-handler [request]
  (let [out (PipedOutputStream.)
        in (PipedInputStream. out)
        out (gzip-output-stream request out)]
    (future
      (try
        (upload-pack repo (gzip-input-stream request) out)
        (catch Exception e (prn e))
        (finally (.close out))))
    (-> (response in)
        (content-type "application/x-git-upload-pack-result"))))

(defn receive-pack-handler [request]
  (let [repo repo
        out (PipedOutputStream.)
        in (PipedInputStream. out)
        out (gzip-output-stream request out)]
    (future
      (try
        (receive-pack repo (gzip-input-stream request) out)
        (catch Exception e (prn e))
        (finally (.close out))))
    (-> (response in)
        (content-type "application/x-git-receive-pack-result"))))

(def smart-http-routes
  (wrap-no-cache-and-gzip
   (context "/:user/:project" [user project]
     (GET "/info/refs" [] info-refs-handler)
     (POST "/git-upload-pack" [] upload-pack-handler)
     (POST "/git-receive-pack" [] receive-pack-handler))))
