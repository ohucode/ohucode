(ns ohucode.db-test
  (:refer-clojure :exclude [update])
  (:use [clojure.test]
        [ohucode.db]
        [ohucode.password]
        [korma.db]
        [korma.core]))

(deftest db-test
  (testing "now 함수 확인"
    (is (instance? java.sql.Timestamp (now))))
  (testing "signups 테이블 확인"
    (is (seq? (select signups))))
  (testing "signups 레코드 추가, 사용자 가입까지 확인"
    (let [passcode (generate-passcode)
          userid (str "test_" passcode)
          email (str userid "@test.com")
          cnt-signup
          (fn [] (-> (select signups (aggregate (count :*) :count))
                     first :count))]
      (transaction
       (let [cnt (cnt-signup)]
         (clean-insert-signup email userid passcode)
         (is (= (inc cnt) (cnt-signup)) "새 레코드가 추가 됐어야 해요.")
         (is (= passcode (signup-passcode email userid)))
         ;; 같은 키의 경우 기존 레코드를 업데이트 합니다.
         (clean-insert-signup email userid (generate-passcode))
         (is (= (inc cnt) (cnt-signup)) "이전 레코드 삭제하고 추가 됐어야 합니다.")

         )
       (rollback)))))
