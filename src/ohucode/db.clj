(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:use [ohucode.core]
        [misaeng.core]
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

(함수 insert-audit [userid action data]
  (insert audits (values {:userid userid :action action
                          :ip (sqlfn "inet" *client-ip*)
                          :data (sqlfn "to_json" (json/write-str data))})))

(함수 select-audits []
  (select audits (order :created_at :DESC) (limit 100)))

(defentity signups)

(함수 clean-insert-signup [email userid code]
  (가정 [attrs {:email email :userid userid :code code}]
    (transaction
     (delete signups (where (dissoc attrs :code)))
     (insert signups (values attrs))
     (insert-audit "guest" "reqcode" attrs))))

(함수 signup-passcode [email userid]
  (-> (select signups (where
                       {:email email :userid userid
                        :created_at [> (now (- *passcode-expire-sec*))]}))
      first :code))

(defentity emails)

(함수 email-acceptable? [email]
  (empty? (select emails (where {:email email}))))

(defentity users
  (has-many emails {:fk :user_id}))

(함수 userid-acceptable? [userid]
  (empty? (select users (where {:userid userid}))))

(함수 select-user [userid]
  (-> (select users (where {:userid userid}))
      first))

(함수 select-users []
  (select users (order :created_at :DESC)))

(함수 insert-new-user [{email :email
                        userid :userid
                        password :password
                        code :code
                        username :name
                        :as attrs}]
  {:pre [(not-any? nil? [email userid code username password])]}

  (transaction
   (when (zero? (delete signups (where {:email email :userid userid :code code})))
     (throw (RuntimeException. "code does not match")))
   (insert users (values {:userid userid :email email :name username
                          :password_digest
                          (pw/ohucode-password-digest userid password)}))
   (insert emails (values {:email email :userid userid :verified_at (now)}))
   (insert-audit userid "signup" {:email email})))

(함수 valid-user-password? [userid password]
  (if-let [raw (-> (select-user userid)
                   :password_digest)]
    (가정 [valid? (pw/ohucode-valid-password? userid password raw)]
      (insert-audit userid "login" {:success valid?})
      valid?)
    (do (insert-audit "guest" "login" {:success false :userid userid})
        false)))
