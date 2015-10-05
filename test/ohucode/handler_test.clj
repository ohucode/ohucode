(ns ohucode.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [ohucode.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "/info/refs should response with a proper content-type"
    (let [response (app (mock/request :get "/u/p/info/refs?service=git-upload-pack"))]
      (is (= ((:headers response) "Content-Type")
             "application/x-git-upload-pack-advertisement")))
    (let [response (app (mock/request :get "/u/p/info/refs?service=git-receive-pack"))]
      (is (= ((:headers response) "Content-Type")
             "application/x-git-receive-pack-advertisement")))))
