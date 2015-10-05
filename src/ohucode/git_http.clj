(ns ohucode.git-http
  (:require [ohucode.git :as git])
  (:import (java.io OutputStream ByteArrayOutputStream)
           (org.eclipse.jgit.transport
            UserAgent PacketLineOut
            ReceivePack RefAdvertiser$PacketLineOutRefAdvertiser
            UploadPack)))

(defn advertise [repo ^OutputStream out]
  (let [svc "git-upload-pack"
        plo (PacketLineOut. out)
        pck (RefAdvertiser$PacketLineOutRefAdvertiser. plo)
        up (UploadPack. repo)]
    (try
      (.writeString plo (str "# service=" svc "\n"))
      (.end plo)
      (.sendAdvertisedRefs up pck)
      (finally (.. up getRevWalk close)))))

(UserAgent/set "OhuGit/0.0.1")

(with-open [repo (git/open ".")
            out (ByteArrayOutputStream.)]
  (advertise repo out)
  (print (.toString out)))
