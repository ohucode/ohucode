(ns 오후코드.핸들러-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.핸들러])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]))

(실험정의 앱실험
  (실험 "main route"
    (가정 [응답 (app (mock/request :get "/"))]
      (확인 (= (:status 응답) 200))))

  (실험 "not-found route"
    (가정 [응답 (app (mock/request :get "/invalid/not/found"))]
      (확인 (= (:status 응답) 404)))))

(실험정의 미들웨어실험
  (실험 "EDN 요청 본문 처리"
    (가정 [본문 "{:아이디 \"test-id\" :이메일 \"a@bc.com\"}"
           요청 (-> (mock/request :post "/some-nice-url")
                    (mock/header "Content-Type" "text/edn")
                    (mock/body 본문))]
      ((#'오후코드.핸들러/wrap-edn-params (fn 확인용-핸들러 [요청]
                   (확인 (= "test-id" (get-in 요청 [:params :아이디])))
                   (확인 (= "a@bc.com" (get-in 요청 [:params :이메일]))))) 요청))))
