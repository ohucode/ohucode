(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:use [ohucode.core]
        [미생.기본]
        [korma.db]
        [korma.core])
  (:require [ohucode.password :as pw]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json])
  (:import [java.sql SQLException]))

(정의 ^:private read-edn
  (comp eval read-string slurp))

(defdb dev-db
  (read-edn "conf/db_dev.edn"))

(comment defdb test-db
  (read-edn "conf/db_test.edn"))

(함수 now
  "현재 시각 in java.sql.Timestamp.
  현재 시각에서 [dsec]초 이후 시간."
  ([] (now 0))
  ([dsec] (java.sql.Timestamp. (+ (.getTime (java.util.Date.))
                                  (* 1000 dsec)))))

(defentity audits)

(함수 insert-audit [아이디 행동 데이터]
  (insert audits (values {:userid 아이디 :action 행동
                          :ip (sqlfn "inet" *client-ip*)
                          :data (sqlfn "to_json" (json/write-str 데이터))})))

(함수 select-audits []
  (select audits (order :created_at :DESC) (limit 100)))

(defentity signups)

(함수 clean-insert-signup [email userid code]
  (가정 [attrs {:email email :userid userid :code code}]
    (transaction
     (delete signups (where (dissoc attrs :code)))
     (insert signups (values attrs))
     (insert-audit "guest" "reqcode" attrs))))

(함수 signup-passcode [이메일 아이디]
  (-> (select signups (where
                       {:email 이메일 :userid 아이디
                        :created_at [> (now (- *passcode-expire-sec*))]}))
      첫째 :code))

(defentity emails)

(함수 가용이메일? [이메일]
  (empty? (select emails (where {:email 이메일}))))

(defentity users
  (has-many emails {:fk :user_id}))

(함수 가용아이디? [아이디]
  (empty? (select users (where {:userid 아이디}))))

(함수 select-user [아이디]
  (-> (select users (where {:userid 아이디}))
      첫째))

(함수 select-users []
  (select users (order :created_at :DESC)))

(함수 insert-new-user [{email :email
                        userid :userid
                        password :password
                        code :code
                        username :name
                        :as attrs}]
  {:pre [(not-any? 공? [email userid code username password])]}

  (transaction
   (when (영? (delete signups (where {:email email :userid userid :code code})))
     (throw (RuntimeException. "code does not match")))
   (insert users (values {:userid userid :email email :name username
                          :password_digest
                          (pw/ohucode-password-digest userid password)}))
   (insert emails (values {:email email :userid userid :verified_at (now)}))
   (insert-audit userid "signup" {:email email})))

(함수 valid-user-password? [아이디 비밀번호]
  (만약-가정 [raw (-> (select-user 아이디)
                      :password_digest)]
    (가정 [valid? (pw/ohucode-valid-password? 아이디 비밀번호 raw)]
      (insert-audit 아이디 "login" {:success valid?})
      valid?)
    (묶음 (insert-audit "guest" "login" {:success 거짓 :userid 아이디})
          거짓)))
