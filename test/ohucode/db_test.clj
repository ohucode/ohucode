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
  (testing "signups 레코드 추가"
    (let [passcode (generate-passcode)
          userid (str "test_" passcode)
          email (str userid "@test.com")
          cnt-signup
          (fn [] (-> (select signups (aggregate (count :*) :count))
                     first :count))]
      (transaction {:isolation :read-committed}
       (let [cnt (cnt-signup)]
         (insert-or-update-signup email userid passcode)
         (is (= (inc cnt) (cnt-signup)))
         (is (= passcode (signup-passcode email userid)))
         (insert-or-update-signup email userid (generate-passcode))
         (is (= (inc cnt) (cnt-signup))))
       (rollback)))))
