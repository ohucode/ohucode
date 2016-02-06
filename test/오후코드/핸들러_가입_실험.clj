(ns 오후코드.핸들러-가입-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.핸들러-가입])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ring.middleware.params :refer [wrap-params]]
            [korma.db :refer [transaction rollback]]
            [오후코드.핸들러 :refer [app]]
            [오후코드.db :as db]))

(실험정의 가입-테스트
  (가정 [요청 (합성 (wrap-params 가입-라우트) mock/request)]
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
                         {:이메일 "test001@test.com" :아이디 "test001"})]
             (확인 (= (:status 응답) 200))
         (확인 (string? (db/signup-passcode "test001@test.com" "test001"))))
       (rollback)))))
