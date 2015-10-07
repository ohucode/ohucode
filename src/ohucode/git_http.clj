(ns ohucode.git-http
  (:require [ohucode.git :as git])
  (:import (java.io InputStream OutputStream ByteArrayOutputStream)
           (org.eclipse.jgit.transport
            UserAgent PacketLineOut
            RefAdvertiser$PacketLineOutRefAdvertiser
            ReceivePack PreReceiveHook PostReceiveHook
            UploadPack RefFilter PreUploadHook PostUploadHook)))

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
