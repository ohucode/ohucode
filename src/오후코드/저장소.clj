(ns 오후코드.저장소
  (:use [미생.기본])
  (:require [clojure.string])
  (:import [org.eclipse.jgit.lib Ref Repository FileMode]
           [org.eclipse.jgit.api Git]
           [org.eclipse.jgit.storage.file FileRepositoryBuilder]
           [org.eclipse.jgit.revwalk RevWalk RevTree]
           [org.eclipse.jgit.treewalk TreeWalk]
           [org.eclipse.jgit.treewalk.filter PathFilter]
           [org.eclipse.jgit.api.errors RefNotFoundException]
           [java.io File FileNotFoundException]))

(정의 ^{:dynamic 참
        :doc "저장소를 읽고 쓸 최상위 디렉토리."}
  *저장소위치* "저장소")

(레코드 커서 [아이디 프로젝트명 레프 경로])

(선언 닫기)

(레코드 저장소레코드 [아이디 프로젝트명 리포]
  java.io.Closeable
  (close [this] (닫기 this)))

(함수 저장소경로 [아이디 프로젝트명]
  (clojure.string/join "/" [*저장소위치* 아이디 프로젝트명]))

(함수- 저장소파일 [아이디 프로젝트명]
  (.. (File. (저장소경로 아이디 프로젝트명)) getAbsoluteFile))

(함수 ^저장소레코드 열기 [아이디 프로젝트명]
  "로컬 파일 저장소를 연다."
  (가정 [리포 (.. (FileRepositoryBuilder.)
                  (setGitDir (저장소파일 아이디 프로젝트명))
                  (setMustExist 참)
                  build)]
    (->저장소레코드 아이디 프로젝트명 리포)))

(함수 닫기 [^저장소레코드 저장소]
  (.close (:리포 저장소)))

(함수 ^저장소레코드 생성! [아이디 프로젝트명]
  "로컬 파일 시스템에 빈 bare 저장소를 새로 만든다."
  (가정 [리포 (doto (.. (FileRepositoryBuilder.)
                        setBare
                        (setGitDir (저장소파일 아이디 프로젝트명))
                        build)
                (.create 참))]
    (->저장소레코드 아이디 프로젝트명 리포)))

(함수- rm-rf! [경로]
  (만약 (.isDirectory 경로)
    (run! rm-rf! (.listFiles 경로)))
  (.delete 경로))

;;; TODO: 저장소레코드 사용하는 형태 고민
(정의 삭제! (합성 rm-rf! 저장소파일))

(함수- git-명령 [리포 명령]
  ;; TODO: Repository에도 .close가 있는데, Git에도 .close를 따로 해야하나?
  (with-open [git (Git. 리포)]
    (-> git 명령 .call)))

(함수- ref->hash [ref]
  {:name (.getName ref)
   :object-id (.name (.getObjectId ref))}) ;; 개체-id

(함수 브랜치목록 [{리포 :리포}]
  (->> (git-명령 리포 (memfn branchList))
      (사상 ref->hash)))

(함수 빈저장소? [저장소]
  (빈? (브랜치목록 저장소)))
