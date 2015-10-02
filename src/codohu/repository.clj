(ns codohu.repository
  [:import (org.eclipse.jgit.lib Ref Repository)
   (org.eclipse.jgit.revwalk RevWalk)
   (org.eclipse.jgit.treewalk TreeWalk)])

(defn repository [path]
  (-> (doto (org.eclipse.jgit.storage.file.FileRepositoryBuilder.)
        (.setGitDir (java.io.File. "."))
        (.readEnvironment)
        (.findGitDir))
      (.build)))

