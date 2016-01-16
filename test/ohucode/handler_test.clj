(ns ohucode.handler-test
  (:use [misaeng.core]
        [misaeng.test])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ohucode.handler :refer :all]))

(실험함수 test-app
  (실험 "main route"
    (가정 [response (app (mock/request :get "/"))]
      (확인 (= (:status response) 200))))

  (실험 "not-found route"
    (가정 [response (app (mock/request :get "/invalid/not/found"))]
      (확인 (= (:status response) 404)))))
