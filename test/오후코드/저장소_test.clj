(ns 오후코드.저장소-test
  (:require [clojure
             [set :as s]
             [test :refer :all]]
            [clojure.test.check
             [clojure-test :refer [defspec]]
             [generators :as gen]
             [properties :as prop]]
            [오후코드
             [db :as db]
             [저장소 :refer :all]]))

(defn- rm-rf! [경로]
  (when (.isDirectory 경로)
    (doseq [파일 (.listFiles 경로)]
      (rm-rf! 파일)))
  (.delete 경로))

(defspec 저장소경로-테스트
  (prop/for-all [이름공간 gen/string-ascii
                 프로젝트명 gen/string-ascii
                 저장소위치 gen/string]
                (and (= (str "repo/" 이름공간 "/" 프로젝트명)
                        (저장소-경로 이름공간 프로젝트명))
                     (= (str 저장소위치 "/" 이름공간 "/" 프로젝트명)
                        (binding [*저장소위치* 저장소위치]
                          (저장소-경로 이름공간 프로젝트명))))))

(deftest 저장소-테스트
  (testing "열기"
    (with-open [ㅈ (열기 "test" "fixture")]
      (is (not (nil? ㅈ)))))

  (testing "빈 저장소인지 확인하기"
    (with-open [빈거 (열기 "test" "empty")
                쓴거 (열기 "test" "fixture")]
      (is (true? (빈저장소? 빈거)))
      (is (false? (빈저장소? 쓴거)))))

  (testing "빈 저장소 만들기"
    (삭제! "test" "new-project")
    (with-open [새거 (생성! "test" "new-project")]
      (is (빈저장소? 새거))))

  (testing "브랜치 목록"
    (with-open [빈거 (열기 "test" "empty")
                쓴거 (열기 "test" "fixture")]
      (is (empty? (브랜치목록 빈거)))
      (is (empty? (s/difference #{"refs/heads/master"
                                  "refs/heads/branch-A"
                                  "refs/heads/branch-B"}
                                (map :name (브랜치목록 쓴거)))))))

  (testing "커밋이력"
    (with-open [빈거 (열기 "test" "empty")
                쓴거 (열기 "test" "fixture")]
      (is (empty? (커밋이력 빈거)))
      (is (= ["7eacf26edeb82e0a080c99d49557ed983ed1edc2"
              "01c405f94c7bcd7838d06a3eb2351baca4dac106"]
             (map :id (take-last 2 (커밋이력 쓴거))))))))

(deftest 저장소-미들웨어-테스트
  (testing "저장소 읽는 미들웨어 정상처리"
    (let [요청   {:앱 {:프로젝트 (db/프로젝트-열람 "test" "fixture")}}
          핸들러 (fn [요청] (get-in 요청 [:앱 :저장소]))]
      (is (= [:아이디 :프로젝트명 :리포] (keys ((저장소읽는-미들웨어 핸들러) 요청))))))

  (testing "저장소 읽는 미들웨어 읽지 못할 때 처리"
    (let [요청   {:앱 {:프로젝트 nil}}
          핸들러 (fn [요청] (get-in 요청 [:앱 :저장소]))]
      (is (nil? ((저장소읽는-미들웨어 핸들러) 요청))))))
