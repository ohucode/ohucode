(ns 오후코드.git-test
  (:use [미생.기본]
        [clojure.test]
        [오후코드.git]))

(use-fixtures :each
  (fn [f]
    (def repo (open "fixture/fixture-repo/.git"))
    (try
      (f)
      (finally (.close repo) (ns-unmap *ns* 'repo)))))

(deftest test-repo
  (testing "open returns a Repository"
    (is (instance? org.eclipse.jgit.lib.Repository repo))))
