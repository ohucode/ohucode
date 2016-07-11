(ns 오후코드.핸들러-test
  (:use [clojure.test]
        [오후코드.핸들러])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]))

(deftest 앱테스트
  (testing "main route"
    (let [응답 (앱라우트 (mock/request :get "/"))]
      (is (= (:status 응답) 200))))

  (testing "not-found route"
    (let [응답 (앱라우트 (mock/request :get "/invalid/not/found"))]
      (is (= (:status 응답) 404)))))

(deftest 미들웨어검사
  (testing "EDN 요청 본문 처리"
    (let [본문 "{:아이디 \"test-id\" :이메일 \"a@bc.com\"}"]
      ((#'오후코드.핸들러/edn-파라미터-미들웨어
        (fn 확인용-핸들러 [요청]
          (is (= "test-id" (get-in 요청 [:params :아이디])))
          (is (= "a@bc.com" (get-in 요청 [:params :이메일])))))
       (-> (mock/request :post "/some-nice-url")
           (mock/header "Content-Type" "application/edn")
           (mock/body 본문)))))

  (testing "EDN 응답 처리"
    (let [요청 (-> (mock/request :get "/whatever")
                   (mock/header "Accept" "application/edn"))
          응답본문 {:edn true :status 200}
          핸들러 (constantly {:status 200 :body 응답본문})
          응답 ((#'오후코드.핸들러/edn-응답-미들웨어 핸들러) 요청)]
      (is (re-find #"^application/edn" (get-in 응답 [:headers "Content-Type"])))
      (is (= (pr-str 응답본문) (:body 응답))))))
