(ns 오후코드.핸들러-가입-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.핸들러-가입])
  (:require [ring.mock.request :as mock]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [korma.db :refer [transaction rollback]]
            [오후코드.db :as db]))

(함수 edn-요청 [method uri data]
  {:request-method method :uri uri
   :headers {"Accept" "application/edn"
             "Content-Type" "application/edn"}
   :params data})

(실험정의 가입실험
  (가정 [요청 (합성 가입-라우트 edn-요청)]
    (실험 "신규가입"
      (transaction
       (가정 [응답 (요청 :post "/signup"
                         {:이메일 "test001@test.com" :아이디 "테스트아이디" :비밀번호 "testpass"
                          :성명 "테스트"})]
             (확인 (= (:status 응답) 200)))
       (rollback)))))
