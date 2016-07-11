(ns 오후코드.핸들러-가입-test
  (:require [clojure.test :refer :all]
            [korma.db :refer [rollback transaction]]
            [오후코드.핸들러-가입 :refer :all]))

(defn edn-요청 [method uri data]
  {:request-method method :uri uri
   :headers {"Accept" "application/edn"
             "Content-Type" "application/edn"}
   :params data})

(deftest 가입테스트
  (let [요청 (comp 가입-라우트 edn-요청)]
    (testing "신규가입"
      (transaction
       (let [응답 (요청 :post "/signup"
                        {:이메일 "test001@test.com" :아이디 "테스트아이디" :비밀번호 "testpass"
                         :성명 "테스트"})]
         (is (= (:status 응답) 200)))
       (rollback)))))
