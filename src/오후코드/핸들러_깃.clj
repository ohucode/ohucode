(ns 오후코드.핸들러-깃
  (:require [compojure.core :refer :all]
            [ring.util.response :refer :all]
            [오후코드.기본 :refer :all]
            [오후코드.권한 :as 권한]
            [오후코드.핸들러-유틸 :refer [미들웨어-라우트]]
            [오후코드.저장소 :as 저장소])
  (:import [java.io InputStream OutputStream PipedInputStream PipedOutputStream]
           [java.util.zip GZIPInputStream GZIPOutputStream]
           [org.eclipse.jgit.transport PacketLineOut PreUploadHook ReceivePack RefAdvertiser$PacketLineOutRefAdvertiser RefFilter UploadPack UserAgent]))

(defn advertise [{repo :리포} svc ^OutputStream out]
  (let [plo (PacketLineOut. out)
        pck (RefAdvertiser$PacketLineOutRefAdvertiser. plo)
        up  (cond
              (= svc "git-upload-pack")  (UploadPack. repo)
              (= svc "git-receive-pack") (ReceivePack. repo)
              :else                      (throw (IllegalArgumentException.)))]
    (try
      (.writeString plo (str "# service=" svc "\n"))
      (.end plo)
      (.sendAdvertisedRefs up pck)
      (finally (.. up getRevWalk close)))))

(defn upload-pack [{repo :리포} ^InputStream in ^OutputStream out]
  (let [ref-filter (reify RefFilter
                     (filter [this refs]
                       (prn (str "filtering for" refs))
                       refs))
        pre-hook   (reify PreUploadHook
                     (onBeginNegotiateRound [this up wants cnt-offered] nil)
                     (onEndNegotiateRound [this up wants cnt-common cnt-not-found ready?] nil)
                     (onSendPack [this up wants haves]
                       (prn "pre-hook")
                       (prn up)))
        up         (doto (UploadPack. repo)
                     (.setBiDirectionalPipe false)
                     (.setRefFilter ref-filter)
                     (.setPreUploadHook pre-hook))]
    (.upload up in out nil)))

(defn receive-pack [{repo :리포} ^InputStream in ^OutputStream out]
  (let [rp (doto (ReceivePack. repo)
             (.setBiDirectionalPipe false))]
    (.receive rp in out nil)))

(UserAgent/set "OhuGit/0.0.1")

(defn- no-cache [response]
  (update response :headers merge
          {"Cache-Control" "no-cache, max-age=0, must-revalidate"
           "Expires"       "Fri, 01 Jan 1980 00:00:00 GMT"
           "Pragma"        "no-cache"}))

(defn- gzip-input-stream [request]
  (let [in   (:body request)
        gzip #{"gzip" "x-gzip"}
        enc  (get-in request [:headers "Content-Encoding"])]
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

(defn info-refs-handler [{{svc :service} :params {repo :저장소} :앱 :as request}]
  (if-not (#{"git-receive-pack" "git-upload-pack"} svc)
    {:status 403 :body "not a valid service request"}
    (let [out (PipedOutputStream.)
          in  (PipedInputStream. out)
          out (gzip-output-stream request out)]
      (로그 "info-refs" repo)
      (future (advertise repo svc out) (.close out))
      (-> (response in)
          (content-type (str "application/x-" svc "-advertisement"))))))

(defn upload-pack-handler [request]
  (let [out  (PipedOutputStream.)
        in   (PipedInputStream. out)
        out  (gzip-output-stream request out)
        repo (get-in request [:앱 :저장소])]
    (로그 "upload-pack-handler called" request)
    (future
      (try
        (upload-pack repo (gzip-input-stream request) out)
        (catch Exception e (prn e))
        (finally (.close out))))
    (-> (response in)
        (content-type "application/x-git-upload-pack-result"))))

(defn receive-pack-handler [request]
  (let [repo (get-in request [:앱 :저장소])
        out  (PipedOutputStream.)
        in   (PipedInputStream. out)
        out  (gzip-output-stream request out)]
    (로그 "receive-pack-handler called" request)
    (future
      (try
        (receive-pack repo (gzip-input-stream request) out)
        (catch Exception e (prn e))
        (finally (.close out))))
    (-> (response in)
        (content-type "application/x-git-receive-pack-result"))))


(defn smart-http-미들웨어 [핸들러 이름공간 프로젝트명]
  (-> 핸들러
      저장소/저장소읽는-미들웨어
      (권한/플젝읽는-미들웨어 이름공간 프로젝트명)))

;; GIT-CLI에서 한글 경로가 안 먹는듯
;; $ nc -l 11000
;; GET /애월조단/첫프로젝트/info/refs?service=git-upload-pack HTTP/1.1
;; Host: 0.0.0.0:11000
;; User-Agent: git/2.8.2
;; Accept: */*
;; Accept-Encoding: gzip
;; Pragma: no-cache
(def smart-http-라우트
  (wrap-no-cache-and-gzip
   (context "/:이름공간/:프로젝트명" [이름공간 프로젝트명]
     (미들웨어-라우트 (smart-http-미들웨어 이름공간 프로젝트명)
                      (GET  "/info/refs" [] info-refs-handler)
                      (POST "/git-upload-pack" [] upload-pack-handler)
                      (POST "/git-receive-pack" [] receive-pack-handler)))))
