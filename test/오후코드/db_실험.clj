(ns 오후코드.db-실험
  (:refer-clojure :exclude [update])
  (:use [미생.기본]
        [미생.실험]
        [오후코드.기본]
        [오후코드.db]
        [오후코드.보안]
        [korma.db]
        [korma.core]))

(매크로 가입-트랜잭션 [bindings & body]
  `(가정 [아이디# (str (gensym "test_"))
          이메일# (str 아이디# "@test.com")
          비밀번호# (str "pass_" 아이디#)]
     (korma.db/transaction
      (try
        (가정 [[~@bindings] [이메일# 아이디# 비밀번호#]]
          ~@body)
        (finally (korma.db/rollback))))))

(실험정의 db-test
  (실험 "now 함수 확인"
    (가정 [n (now) n-1m (now -60)]
      (확인* [t] (instance? java.sql.Timestamp t) n n-1m)
      (확인 (> 10
               (Math/abs (- (.getTime n)
                            (.getTime n-1m)
                            60000))))))

  (실험 "사용자 신규 가입"
    (가입-트랜잭션
     [이메일 아이디 비밀번호]
     (신규가입 {:이메일 이메일 :아이디 아이디 :비밀번호 비밀번호 :성명 "테스트"})
     (확인 (= (ohucode-password-digest 아이디 비밀번호)
              (:비번해쉬 (select-user 아이디)))
           "패스워드 해시 보관")
     (확인 (valid-user-password? 아이디 비밀번호) "패스워드 확인")
     (확인 (not (가용이메일? 이메일)) "가입한 이메일은 사용할 수 없어야한다")))

  (실험 "대소문자 아이디 구분"
    (transaction
     (insert 이용자 (values {:아이디 "Aaa" :이메일 "test@abc.com"}))
     (확인 (예외발생? Exception
                      (insert 이용자 (values {:아이디 "aaa" :이메일 "another@abc.com"}))))
     (rollback))))
