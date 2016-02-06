(ns 오후코드.git-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.git]))

(실험설정 :each
  (fn [f]
    (정의 repo (open "fixture/fixture-repo/.git"))
    (try
      (f)
      (finally (.close repo) (ns-unmap *ns* 'repo)))))

(실험정의 test-repo
  (실험 "open returns a Repository"
    (확인 (인스턴스? org.eclipse.jgit.lib.Repository repo))))
