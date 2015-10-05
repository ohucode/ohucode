(ns ohucode.git-test
  (:require [clojure.test :refer :all]
            [ohucode.git :refer :all]))

(use-fixtures :each
  (fn [f]
    (def repo (open "fixture/git.git"))
    (try
      (f)
      (finally (.close repo) (ns-unmap *ns* 'repo)))))

(deftest test-repo
  (testing "open returns a Repository"
    (is (instance? org.eclipse.jgit.lib.Repository repo))))
