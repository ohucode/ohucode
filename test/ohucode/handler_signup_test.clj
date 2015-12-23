(ns ohucode.handler-signup-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ohucode.handler :refer [app]]
            [ohucode.handler-signup :refer :all]))

(deftest test-signup
  (let [req (comp app
                  ;; TODO: 중간 다른 미들웨어에서 세션 값을 바꾸는 것 같다.
                  (fn [r] (pr r) r)
                  #(assoc-in % [:session :ring.middleware.anti-forgery/anti-forgery-token] "test")
                  #(assoc-in % [:headers "x-csrf-token"] "test")
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
      (let [res (req :post "/signup/2" {:userid "test"})]
        (println res)
        (is (= (:status res) 200))))))
