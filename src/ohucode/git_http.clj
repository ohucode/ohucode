(ns ohucode.git-http
  (:require [ohucode.git :as git])
  (:import (java.io OutputStream ByteArrayOutputStream)
           (org.eclipse.jgit.transport
            UserAgent
            ReceivePack PacketLineOut RefAdvertiser$PacketLineOutRefAdvertiser)))

(defn advertise [repo ^OutputStream out]
  (let [svc "git-receive-pack"
        plo (PacketLineOut. out)
        pck (RefAdvertiser$PacketLineOutRefAdvertiser. plo)
        rp (ReceivePack. repo)]
    (try
      (.writeString plo (str "# service=" svc "\n"))
      (.end plo)
      (.sendAdvertisedRefs rp pck)
      (finally (.. rp getRevWalk close)))))

(UserAgent/set "OhuGit/0.0.1")

(with-open [repo (git/open ".")
            out (ByteArrayOutputStream.)]
  (advertise repo out)
  (print (.toString out)))
