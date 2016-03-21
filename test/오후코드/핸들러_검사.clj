(ns 오후코드.핸들러-검사
  (:use [미생.기본]
        [미생.검사]
        [오후코드.핸들러])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]))

(검사정의 앱검사
  (검사 "main route"
    (가정 [응답 (앱라우트 (mock/request :get "/"))]
      (확인 (= (:status 응답) 200))))

  (검사 "not-found route"
    (가정 [응답 (앱라우트 (mock/request :get "/invalid/not/found"))]
      (확인 (= (:status 응답) 404)))))

(검사정의 미들웨어검사
  (검사 "EDN 요청 본문 처리"
    (가정 [본문 "{:아이디 \"test-id\" :이메일 \"a@bc.com\"}"]
      ((#'오후코드.핸들러/edn-파라미터-미들웨어
        (fn 확인용-핸들러 [요청]
          (확인 (= "test-id" (get-in 요청 [:params :아이디])))
          (확인 (= "a@bc.com" (get-in 요청 [:params :이메일])))))
       (-> (mock/request :post "/some-nice-url")
           (mock/header "Content-Type" "application/edn")
           (mock/body 본문)))))

  (검사 "EDN 응답 처리"
    (가정 [요청 (-> (mock/request :get "/whatever")
                    (mock/header "Accept" "application/edn"))
           응답본문 {:edn true :status 200}
           핸들러 (constantly {:status 200 :body 응답본문})
           응답 ((#'오후코드.핸들러/edn-응답-미들웨어 핸들러) 요청)]
      (확인 (re-find #"^application/edn" (get-in 응답 [:headers "Content-Type"])))
      (확인 (= (pr-str 응답본문) (:body 응답))))))
