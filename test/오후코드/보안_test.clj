(ns 오후코드.보안-test
  (:require [오후코드.보안 :refer :all]
            [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defspec base64-test
  (prop/for-all [v gen/bytes]
                (= (seq v) (seq (decode-base64 (encode-base64 v))))))

(defspec urlsafe-base64-test
  (prop/for-all [v gen/bytes]
                (= (seq v) (seq (decode-urlsafe-base64 (encode-urlsafe-base64 v))))))

(defspec 해쉬찍고-확인
  (prop/for-all [password (gen/fmap (partial str "pass-") gen/string-ascii)
                 salt (gen/fmap (partial str "salt-") gen/string-ascii)]
                (valid-password-digest? password salt (password-digest password salt))))

(deftest 비밀번호-검사
  (testing "pbkdf2"
    (are [e p] (= e p)
      "3uulPEWmqchqOCHvBhN1QCktTXw=" (pbkdf2 "password" "salt1234" 10000 160)
      "ANUboDznZ/HMkydrYS9DpI1PCEM=" (pbkdf2 "password" "0000salt" 10000 160)
      "uO/Sjsqe6gn9Sq0xQi5HFcK9LbA=" (pbkdf2 "other"    "salt1234" 10000 160)
      "u7xbj9BKiiA26csQfSH6jKAZg1c=" (pbkdf2 "other"    "0000salt" 10000 160)))

  (testing "random-6-digits로 여러번 만들어 보고, 중복된 것 없는지 확인"
    (let [xs (repeatedly 100 generate-passcode)]
      (is (= (count xs) (count (set xs))))))

  (testing "random-digest로 여러번 만들어보고 중복 없는지 확인"
    (let [xs (repeatedly 10000 random-digest)]
      (is (= (count xs) (count (set xs))))))

  (testing "사이트 특정 salt 테스트"
    (dotimes [i 5]
      (let [r (random-int)
            아이디 (str "test" r)
            비밀번호 (str "pass" r)]
        (is (오후코드-유효비번?
             아이디 비밀번호
             (오후코드-비번해쉬 아이디 비밀번호)))))))

(deftest 서명-검사
  (testing "기본키로 서명하고 확인하기"
    (dotimes [i 3]
      (let [본문 (random-digest)
            서명 (서명 본문)]
        (is (true? (서명확인 본문 서명))))))

  (testing "임의키 만들어서 서명하고 확인하기"
    (dotimes [i 3]
      (let [키쌍 (키쌍생성 512)]
        (binding [*개인키* (.getPrivate 키쌍)
                  *공개키* (.getPublic 키쌍)]
          (let [본문 (random-digest)
                서명 (서명 본문)]
            (is (true? (서명확인 본문 서명))))))))

  (testing "기본키로 서명한 것과 별도키로 서명한 값은 다르다"
    (let [본문     (random-digest)
          기본서명 (서명 본문)
          키쌍     (키쌍생성 2048)]
      (binding [*개인키* (.getPrivate 키쌍)
                *공개키* (.getPublic 키쌍)]
        (is (not= 기본서명 (서명 본문)))
        (is (true? (서명확인 본문 (서명 본문))))
        (is (false? (서명확인 본문 기본서명)))))))

(deftest 인증쿠키-검사
  (testing "인증쿠키 만들고, 해석해 보기"
    (let [인증 {:아이디 "애월조단" :발급일시 (java.util.Date.)
                :만료일시 (java.util.Date.)}
          토큰 (인증토큰생성 인증)]
      (is (= 인증 (인증토큰확인 토큰)))
      (is (nil? (인증토큰확인 (str 토큰 "토큰망치기")))))))
