(ns ohucode.handler-signup-test
  (:use [misaeng.core]
        [misaeng.test]
        [ohucode.handler-signup])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ring.middleware.params :refer [wrap-params]]
            [korma.db :refer [transaction rollback]]
            [ohucode.handler :refer [app]]
            [ohucode.db :as db]
            [ohucode.db-test :as db-test]))

(실험함수 가입-테스트
  (가정 [요청 (조합 (wrap-params 가입-라우트)
                    mock/request)]
    (주석 testing "show step1"
      (가정 [응답 (요청 :get "/signup")]
        (확인 (= (:status 응답) 200))))

    (주석 testing "금지한 아이디 확인"
      (doseq [금지어 (가짐 3 (섞기 금지아이디))]
        (확인 (= (:status
                  (요청 :get (str "/signup/userid/" 금지어)))
                 409))))

    (실험 "step1: 확인코드 신청"
      (transaction
       (가정 [응답 (요청 :post "/signup"
                         {:email "test001@test.com" :userid "test001"})]
             (확인 (= (:status 응답) 200))
         (확인 (string? (db/signup-passcode "test001@test.com" "test001"))))
       (rollback)))))
