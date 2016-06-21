(ns 오후코드.저장소
  (:import [java.io File]
           [org.eclipse.jgit.api Git]
           [org.eclipse.jgit.storage.file FileRepositoryBuilder]
           [org.eclipse.jgit.api.errors RefNotFoundException]))

(def ^{:dynamic true
       :doc "저장소를 읽고 쓸 최상위 디렉토리."}
  *저장소위치* "저장소")

(defrecord 커서 [아이디 프로젝트명 레프 경로])

(declare 닫기)

(defrecord 저장소레코드 [아이디 프로젝트명 리포]
  java.io.Closeable
  (close [this] (닫기 this)))

(defn 저장소경로 [아이디 프로젝트명]
  (clojure.string/join "/" [*저장소위치* 아이디 프로젝트명]))

(defn- 저장소파일 [아이디 프로젝트명]
  (.. (File. (저장소경로 아이디 프로젝트명)) getAbsoluteFile))

(defn ^저장소레코드 열기 [아이디 프로젝트명]
  "로컬 파일 저장소를 연다."
  (let [리포 (.. (FileRepositoryBuilder.)
                 (setGitDir (저장소파일 아이디 프로젝트명))
                 (setMustExist true)
                 build)]
    (->저장소레코드 아이디 프로젝트명 리포)))

(defn 닫기 [^저장소레코드 저장소]
  (.close (:리포 저장소)))

(defn ^저장소레코드 생성! [아이디 프로젝트명]
  "로컬 파일 시스템에 빈 bare 저장소를 새로 만든다."
  (let [디렉터리 (저장소파일 아이디 프로젝트명)
        리포 (doto (.. (FileRepositoryBuilder.)
                       setBare
                       (setGitDir 디렉터리)
                       build)
               (.create true))]
    (->저장소레코드 아이디 프로젝트명 리포)))

(defn- rm-rf! [경로]
  (if (.isDirectory 경로)
    (run! rm-rf! (.listFiles 경로)))
  (.delete 경로))

;;; TODO: 저장소레코드 사용하는 형태 고민
(def 삭제! (comp rm-rf! 저장소파일))

(defn- git-명령 [리포 명령]
  ;; TODO: Repository에도 .close가 있는데, Git에도 .close를 따로 해야하나?
  (with-open [git (Git. 리포)]
    (-> git 명령 .call)))

(defn- resolve-ref [리포 레프]
  (if-let [object-id (.resolve 리포 레프)]
    object-id
    #_(throw (RefNotFoundException. 레프))))

(defn- ref->hash [ref]
  {:name (.getName ref)
   :object-id (.name (.getObjectId ref))}) ;; 개체-id

(defn 브랜치목록 [{리포 :리포}]
  (->> (git-명령 리포 (memfn branchList))
       (map ref->hash)))

(defn 기본브랜치명 [리포]
  (:name (first (브랜치목록 리포))))

(defn 커밋이력
  ([리포] (커밋이력 리포 "HEAD"))
  ([{리포 :리포} 레프]
   (if-let [object-id (resolve-ref 리포 레프)]
     (seq (git-명령 리포 #(-> (.log %) (.add object-id)))))))

(defn 빈저장소? [저장소]
  (empty? (브랜치목록 저장소)))
