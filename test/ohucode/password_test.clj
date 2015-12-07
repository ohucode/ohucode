(ns ohucode.password-test
  (:use [clojure.test]
        [ohucode.password]))

(deftest password-test
  (testing "pbkdf2"
    (are [e p] (= e p)
      "kSZ3JZ6ov0v5HVwjzHz4pQg8+x8=" (pbkdf2 "password" "salt1234" 100000 160)
      "U0WSjbIsUg6DvHVmGi6oRXr/KSA=" (pbkdf2 "password" "0000salt" 100000 160)
      "tOE5IC6zqHxfbexkOfSIQtyqknA=" (pbkdf2 "other"    "salt1234" 100000 160)
      "/11bL9NO6j7Ho7ZLw929RfZve0Y=" (pbkdf2 "other"    "0000salt" 100000 160)))

  (testing "digest and validate"
    (dotimes [i 5]
      (let [password (str "password-" (random-int))
            salt (str "salt-" (random-int))
            raw (password-digest password salt)]
        (is (valid-password-digest? raw password salt)))))

  (testing "random-6-digits should have no duplicates in a limited condition"
    (let [xs (repeatedly 100 generate-passcode)]
      (is (= (count xs) (count (set xs))))))

  (testing "random-digest should have no duplicates in a limited condition"
    (let [xs (repeatedly 10000 random-digest)]
      (is (= (count xs) (count (set xs)))))))
