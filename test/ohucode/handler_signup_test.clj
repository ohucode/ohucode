(ns ohucode.handler-signup-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ohucode.handler-signup :refer :all]))

(deftest test-signup
  (testing "show step1"
    (let [res (signup-routes (mock/request :get "/signup"))]
      (is (= (:status res) 200))))

  (testing "금지한 아이디 확인"
    (doseq [restricted (take 3 (shuffle restricted-userids))]
      (is (= (:status
              (signup-routes (mock/request :get (str "/signup/userid/" restricted))))
             409)))))
