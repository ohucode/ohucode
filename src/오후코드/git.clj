(ns 오후코드.git
  (:use [미생.기본])
  (:import (org.eclipse.jgit.lib Ref Repository FileMode)
           (org.eclipse.jgit.api Git)
           (org.eclipse.jgit.storage.file FileRepositoryBuilder)
           (org.eclipse.jgit.revwalk RevWalk RevTree)
           (org.eclipse.jgit.treewalk TreeWalk)
           (org.eclipse.jgit.treewalk.filter PathFilter)
           (org.eclipse.jgit.api.errors RefNotFoundException)
           (java.io File FileNotFoundException)))

(defrecord Cursor [owner project head path])

(함수 ^Repository open [path]
  "build a local FileRepository from the path"
  (.build (doto (FileRepositoryBuilder.)
            (.readEnvironment)
            (.findGitDir (.getAbsoluteFile (File. path))))))

(함수 ^RevTree rev-tree [repo refname]
  (가정 [walk (RevWalk. repo)]
    (try
      (->> (.getRef repo refname)
           (.getObjectId)
           (.parseCommit walk)
           (.getTree)
           (.getId)
           (.parseTree walk))
      (finally
        (.dispose walk)))))

(함수 ^TreeWalk tree-walk
  ([repo ^String refname]
   (if-let [rev (rev-tree repo refname)]
     (doto (TreeWalk. repo)
       (.addTree (.getId rev))
       (.setRecursive false))
     (throw (RefNotFoundException. refname))))
  ([repo ^String refname ^String path]
   (if-let [rev (rev-tree repo refname)]
     (if-let [walk (TreeWalk/forPath repo path rev)]
       (do
         (if (.isSubtree walk) (.enterSubtree walk))
         walk)
       (throw (FileNotFoundException. path)))
     (throw (RefNotFoundException. refname)))))

(함수- tree-walk->map [^TreeWalk walk]
  {:name (.getNameString walk)
   :object-id (.name (.getObjectId walk 0))
   :tree? (= FileMode/TREE (.getFileMode walk 0))})

(함수 tree
  ([repo path]
   (tree repo "HEAD" path))
  ([repo ref path]
   (tree repo ref path tree-walk->map))
  ([repo ref path conv]
   (with-open [walk (만약 (empty? path)
                      (tree-walk repo ref)
                      (tree-walk repo ref path))]
     (loop [r []]
       (if (.next walk)
         (recur (conj r (conv walk)))
         r)))))

(함수 blob [repo ref path]
  "특정 커밋 해당 경로에 있는 파일의 기본 정보와 InputStream을 얻는다"
  (with-open [walk (tree-walk repo ref path)]
    (가정 [sha (.getObjectId walk 0)
          loader (.open repo sha)]
      {:size (.getSize loader)
       :type (.getType loader)
       :mode (str (.getFileMode walk 0))
       :stream (.openStream loader)})))

;;(println  (blob (open ".git") "master" "src/ohucode/repository.clj"))

(함수- git-command [repo commandf]
  (with-open [git (Git. repo)]
    (-> git commandf .call)))

(함수 log [repo ref]
  (if-let [object-id (.resolve repo ref)]
    (git-command repo #(-> (.log %) (.add object-id)))
    (throw (RefNotFoundException. ref))))

(함수 recent-commit-for-path [repo ref path]
  (if-let [object-id (.resolve repo ref)]
    (-> (git-command repo #(-> (.log %)
                               (.add object-id)
                               (.addPath path)
                               (.setMaxCount 1)))
        .iterator
        .next)
    (throw (RefNotFoundException. ref))))


(함수- ref->hash [ref]
  {:name (.getName ref)
   :object-id (.name (.getObjectId ref))})

(함수 branches [repo]
  (맵 ref->hash
      (git-command repo (memfn branchList))))

(함수 tags [repo]
  (맵 ref->hash
       (git-command repo (memfn tagList))))
