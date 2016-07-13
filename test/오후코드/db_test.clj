(ns 오후코드.db-test
  (:refer-clojure :exclude [update])
  (:use [clojure.test]
        [오후코드.기본]
        [오후코드.db]
        [오후코드.보안]
        [korma.db]
        [korma.core]))

(defmacro 롤백-트랜잭션 [& 본문]
  `(korma.db/transaction
    (try
      ~@본문
      (finally (korma.db/rollback)))))

(defmacro 가입-트랜잭션 [bindings & body]
  `(let [아이디# (str (gensym "test_"))
         이메일# (str 아이디# "@test.com")
         비밀번호# (str "pass_" 아이디#)]
     (롤백-트랜잭션
      (let [[~@bindings] [이메일# 아이디# 비밀번호#]]
        ~@body))))

(deftest db-test
  (testing "now 함수 확인"
    (let [n (now) n-1m (now -60)]
      (are [t] (instance? java.sql.Timestamp t) n n-1m)
      (is (> 10
             (Math/abs (- (.getTime n)
                          (.getTime n-1m)
                          60000))))))

  (testing "사용자 신규 가입"
    (가입-트랜잭션
     [이메일 아이디 비밀번호]
     (신규가입 {:이메일 이메일 :아이디 아이디 :비밀번호 비밀번호 :성명 "테스트"})
     (is (= (오후코드-비번해쉬 아이디 비밀번호)
            (:비번해쉬 (이용자-열람 아이디)))
         "패스워드 해시 보관")
     (is (유효-비밀번호? 아이디 비밀번호) "패스워드 확인")
     (is (not (가용이메일? 이메일)) "가입한 이메일은 사용할 수 없어야한다")))

  (testing "대소문자 아이디 구분"
    (롤백-트랜잭션
     (insert 이용자 (values {:아이디 "Aaa" :이메일 "test@abc.com"}))
     (is (thrown? Exception
                  (insert 이용자 (values {:아이디 "aaa" :이메일 "another@abc.com"})))))))

(deftest 프로젝트-검사
  (testing "프로젝트 생성"
    (롤백-트랜잭션
     (let [아이디 "test"]
       (프로젝트-생성 아이디 "첫프로젝트" "설명도 넣어야겠지요?" true)
       (is (= {:소유자 아이디 :이름 "첫프로젝트" :공개 true}
              (-> (select 프로젝트 (where {:소유자 아이디 :이름 "첫프로젝트"}))
                  first
                  (select-keys [:소유자 :이름 :공개])))))))

  (testing "프로젝트 열람"
    (let [아이디 "test" 프로젝트명 "empty"]
      (is (= {:소유자 아이디 :이름 프로젝트명 :공개 true}
             (select-keys (프로젝트-열람 아이디 프로젝트명)
                          [:소유자 :이름 :공개])))))

  (testing "프로젝트 목록"
    (롤백-트랜잭션
     (let [아이디 "test"]
       (is (= #{"private" "empty" "fixture"}
              (set (map :이름 (프로젝트-목록 아이디)))))
       (is (empty? (프로젝트-목록 "admin")))))))
