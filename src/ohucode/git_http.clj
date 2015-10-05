(ns ohucode.git-http
  (:require [ohucode.git :as git])
  (:import (java.io OutputStream ByteArrayOutputStream)
           (org.eclipse.jgit.transport
            UserAgent PacketLineOut
            ReceivePack RefAdvertiser$PacketLineOutRefAdvertiser
            UploadPack)))

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

(UserAgent/set "OhuGit/0.0.1")

(with-open [repo (git/open ".")
            out (ByteArrayOutputStream.)]
  (advertise repo "git-upload-pack" out)
  (print (.toString out)))
