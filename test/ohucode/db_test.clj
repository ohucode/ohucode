(ns ohucode.db-test
  (:refer-clojure :exclude [update])
  (:use [clojure.test]
        [ohucode.db]
        [korma.core]))

(deftest db-test
  (testing "now 함수 확인"
    (is (instance? java.sql.Timestamp (now))))
  (testing "signups 테이블 확인"
    (is (seq? (select signups))))
  (testing "signups 레코드 추가"
    (is ())))
