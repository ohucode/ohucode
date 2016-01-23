(ns 오후코드.핸들러-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.핸들러])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]))

(실험함수 앱실험
  (실험 "main route"
    (가정 [응답 (app (mock/request :get "/"))]
      (확인 (= (:status 응답) 200))))

  (실험 "not-found route"
    (가정 [응답 (app (mock/request :get "/invalid/not/found"))]
      (확인 (= (:status 응답) 404)))))
