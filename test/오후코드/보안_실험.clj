(ns 오후코드.보안-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.보안]))

(실험정의 password-실험
  (실험 "pbkdf2"
    (확인* [e p] (= e p)
      "kSZ3JZ6ov0v5HVwjzHz4pQg8+x8=" (pbkdf2 "password" "salt1234" 100000 160)
      "U0WSjbIsUg6DvHVmGi6oRXr/KSA=" (pbkdf2 "password" "0000salt" 100000 160)
      "tOE5IC6zqHxfbexkOfSIQtyqknA=" (pbkdf2 "other"    "salt1234" 100000 160)
      "/11bL9NO6j7Ho7ZLw929RfZve0Y=" (pbkdf2 "other"    "0000salt" 100000 160)))

  (실험 "digest and validate"
    (누차 [i 3]
      (가정 [password (str "password-" (random-int))
             salt (str "salt-" (random-int))
             raw (password-digest password salt)]
        (확인 (valid-password-digest? password salt raw)))))

  (실험 "random-6-digits should have no duplicates in a limited condition"
    (가정 [xs (반복해서 100 generate-passcode)]
      (확인 (= (개수 xs) (개수 (집합 xs))))))

  (실험 "random-digest should have no duplicates in a limited condition"
    (가정 [xs (반복해서 10000 random-digest)]
      (확인 (= (개수 xs) (개수 (집합 xs))))))

  (실험 "사이트 특정 salt 테스트"
    (누차 [i 5]
      (가정 [r (random-int)
             userid (str "test" r)
             password (str "pass" r)]
        (확인 (ohucode-valid-password?
               userid password
               (ohucode-password-digest userid password)))))))
