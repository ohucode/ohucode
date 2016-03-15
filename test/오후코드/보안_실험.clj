(ns 오후코드.보안-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.보안]))

(실험정의 비밀번호-실험
  (실험 "pbkdf2"
    (확인* [e p] (= e p)
      "kSZ3JZ6ov0v5HVwjzHz4pQg8+x8=" (pbkdf2 "password" "salt1234" 100000 160)
      "U0WSjbIsUg6DvHVmGi6oRXr/KSA=" (pbkdf2 "password" "0000salt" 100000 160)
      "tOE5IC6zqHxfbexkOfSIQtyqknA=" (pbkdf2 "other"    "salt1234" 100000 160)
      "/11bL9NO6j7Ho7ZLw929RfZve0Y=" (pbkdf2 "other"    "0000salt" 100000 160)))

  (실험 "해쉬찍고 확인"
    (누차 [i 3]
      (가정 [password (str "password-" (random-int))
             salt (str "salt-" (random-int))
             raw (password-digest password salt)]
        (확인 (valid-password-digest? password salt raw)))))

  (실험 "random-6-digits로 여러번 만들어 보고, 중복된 것 없는지 확인"
    (가정 [xs (반복해서 100 generate-passcode)]
      (확인 (= (개수 xs) (개수 (집합 xs))))))

  (실험 "random-digest로 여러번 만들어보고 중복 없는지 확인"
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

(실험정의 서명-실험
  (실험 "기본키로 서명하고 확인하기"
    (누차 [i 5]
      (가정 [본문 (random-digest)
             서명 (서명 본문)]
        (확인 (참? (서명확인 본문 서명))))))

  (실험 "임의키 만들어서 서명하고 확인하기"
    (누차 [i 5]
      (가정 [키쌍 (키쌍생성)]
        (바인딩 [*개인키* (.getPrivate 키쌍)
                 *공개키* (.getPublic 키쌍)])
        (가정 [본문 (random-digest)
               서명 (서명 본문)]
          (확인 (참? (서명확인 본문 서명)))))))

  (실험 "기본키로 서명한 것과 별도키로 서명한 값은 다르다"
    (가정 [본문     (random-digest)
           기본서명 (서명 본문)
           키쌍     (키쌍생성)]
      (바인딩 [*개인키* (.getPrivate 키쌍)
               *공개키* (.getPublic 키쌍)]
        (확인 (= 기본서명 (서명 본문)))))))
