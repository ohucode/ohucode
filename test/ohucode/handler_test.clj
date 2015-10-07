(ns ohucode.handler-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ohucode.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "project home"
    (let [response (app (mock/request :get "/u/p"))]
      (is (= (:status response) 200))
      (is (.contains (:body response) "u/p"))))
  
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

