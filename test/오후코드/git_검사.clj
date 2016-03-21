(ns 오후코드.git-검사
  (:use [미생.기본]
        [미생.검사]
        [오후코드.git]))

(검사설정 :each
  (fn [f]
    (정의 repo (open "fixture/fixture-repo/.git"))
    (try
      (f)
      (finally (.close repo) (ns-unmap *ns* 'repo)))))

(검사정의 test-repo
  (검사 "open returns a Repository"
    (확인 (인스턴스? org.eclipse.jgit.lib.Repository repo))))
