(ns ohucode.db-test
  (:refer-clojure :exclude [update])
  (:use [misaeng.core]
        [clojure.test]
        [ohucode.core]
        [ohucode.db]
        [ohucode.password]
        [korma.db]
        [korma.core]))

(defmacro signup-transaction [bindings & body]
  `(let [code# (ohucode.password/generate-passcode)
         userid# (str "test_" code#)
         email# (str userid# "@test.com")]
     (korma.db/transaction
      (try
        (ohucode.db/clean-insert-signup email# userid# code#)
        (let [[~@bindings] [email# userid# code#]]
          ~@body)
        (finally (korma.db/rollback))))))

(deftest db-test
  (testing "now 함수 확인"
    (let [n (now) n-1m (now -60)]
      (are [t] (instance? java.sql.Timestamp t) n n-1m)
      (is (> 5
             (Math/abs (- (.getTime n)
                          (.getTime n-1m)
                          60000))))))

  (testing "signups 테이블 확인"
    (is (seq? (select signups))))

  (testing "가입 이메일 인증코드 유효시간 처리"
    (binding [*passcode-expire-sec* -10]
      (signup-transaction [email userid code]
                          (is (nil? (signup-passcode email userid))))))
  (testing "signups 레코드 추가"
    (let [count-signup
          (fn [] (-> (select signups (aggregate (count :*) :count))
                     first :count))
          cnt (count-signup)]
      (signup-transaction [email userid code]
                          (is (= (inc cnt) (count-signup)) "새 레코드가 추가 됐어야 해요.")
                          (is (= code) (signup-passcode email userid))
                          (clean-insert-signup email userid (generate-passcode))
                          (is (= (inc cnt) (count-signup)) "이전 레코드 삭제하고 추가 됐어야 합니다.")
                          (is (not= code (signup-passcode email userid))))))

  (testing "code 틀린 사용자 신규 가입"
    (is (thrown? Exception
                 (signup-transaction
                  [email userid code]
                  (insert-new-user {:code "not-a-valid-code" :email email :userid userid
                                    :name "코드틀린유저" :password "anything"})))))

  (testing "사용자 신규 가입"
    (signup-transaction
     [email userid code]
     (let [password (str "pass" code)]
       (insert-new-user {:code code :email email :userid userid
                         :name "테스트유저" :password password})
       (is (nil? (signup-passcode email userid)) "가입 신청 정보는 삭제합니다")
       (is (not= password (:password_digest (select-user userid))) "패스워드 해시 보관")
       (is (valid-user-password? userid password)) "패스워드 확인"))))
