(ns ohucode.handler-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ohucode.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid/not/found"))]
      (is (= (:status response) 404)))))
