(ns ohucode.handler-signup-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ring.middleware.params :refer [wrap-params]]
            [ohucode.handler :refer [app]]
            [ohucode.handler-signup :refer :all]
            [ohucode.db :as db]
            [ohucode.db-test :as db-test]))

(deftest test-signup
  (let [req (comp (wrap-params signup-routes)
                  mock/request)]
    (comment testing "show step1"
      (let [res (req :get "/signup")]
        (is (= (:status res) 200))))

    (comment testing "금지한 아이디 확인"
      (doseq [restricted (take 3 (shuffle restricted-userids))]
        (is (= (:status
                (req :get (str "/signup/userid/" restricted)))
               409))))

    (testing "step1: 확인코드 신청"
      (let [res (req :post "/signup"
                     {:email "test001@test.com" :userid "test001"})]
        (is (= (:status res) 200))
        (is (string? (db/signup-passcode "test001@test.com" "test001")))
        ))))
