(ns 오후코드.db-실험
  (:refer-clojure :exclude [update])
  (:use [미생.기본]
        [미생.실험]
        [오후코드.기본]
        [오후코드.db]
        [오후코드.password]
        [korma.db]
        [korma.core]))

(매크로 signup-transaction [bindings & body]
  `(가정 [코드# (오후코드.password/generate-passcode)
          아이디# (str "test_" 코드#)
          이메일# (str 아이디# "@test.com")
          다이제스트# "some-password"]
     (korma.db/transaction
      (try
        (오후코드.db/clean-insert-signup 이메일# 아이디# 코드# 다이제스트#)
        (가정 [[~@bindings] [이메일# 아이디# 코드# 다이제스트#]]
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

  (실험 "signups 테이블 확인"
    (확인 (seq? (select signups))))

  (실험 "가입 이메일 인증코드 유효시간 처리"
    (바인딩 [*passcode-expire-sec* -10]
      (signup-transaction [email userid code digest]
                          (확인 (nil? (signup-passcode email userid))))))
  (실험 "signups 레코드 추가"
    (가정 [count-signup
          (fn [] (-> (select signups (aggregate (count :*) :count))
                     first :count))
          cnt (count-signup)]
      (signup-transaction [email userid code digest]
                          (확인 (= (증가 cnt) (count-signup)) "새 레코드가 추가 됐어야 해요.")
                          (확인 (= code) (signup-passcode email userid))
                          (clean-insert-signup email userid (generate-passcode) digest)
                          (확인 (= (증가 cnt) (count-signup)) "이전 레코드 삭제하고 추가 됐어야 합니다.")
                          (확인 (not= code (signup-passcode email userid))))))

  (실험 "code 틀린 사용자 신규 가입"
    (확인 (thrown? Exception
                   (signup-transaction
                    [email userid code digest]
                    (insert-new-user {:code "not-a-valid-code" :email email :userid userid
                                      :name "코드틀린유저" :password "anything"})))))

  (실험 "사용자 신규 가입"
    (signup-transaction
     [email userid code digest]
     (가정 [password (str "pass" code)]
       (insert-new-user {:code code :email email :userid userid
                         :name "테스트유저" :password password})
       (확인 (nil? (signup-passcode email userid)) "가입 신청 정보는 삭제합니다")
       (확인 (not= password (:password_digest (select-user userid))) "패스워드 해시 보관")
       (확인 (valid-user-password? userid password)) "패스워드 확인"))))
