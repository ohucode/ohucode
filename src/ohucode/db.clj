(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:require [ohucode.password :as pw]
            [taoensso.timbre :as timbre]
            [korma.db :refer :all]
            [korma.core :refer :all])
  (:import [java.sql SQLException]))

(def ^:private read-edn
  (comp eval read-string slurp))


(defdb dev-db
  (read-edn "conf/db_dev.edn"))

(comment defdb test-db
  (read-edn "conf/db_test.edn"))

(defn now
  "현재 시각 in java.sql.Timestamp.
  현재 시각에서 [dsec]초 이후 시간."
  ([] (now 0))
  ([dsec] (java.sql.Timestamp. (+ (.getTime (java.util.Date.))
                                  (* 1000 dsec)))))

(defentity signups)

(defn clean-insert-signup [email userid code]
  (let [key {:email email :userid userid}]
    (transaction
     (delete signups (where key))
     (insert signups (values (assoc key :code code))))))

(def ^:dynamic *passcode-expire-sec* (* 30 60))

(defn signup-passcode [email userid]
  (-> (select signups (where
                       {:email email :userid userid
                        :created_at [> (now (- *passcode-expire-sec*))]}))
      first :code))

(defentity emails)

(defn email-acceptable? [email]
  (empty? (select emails (where {:email email}))))

(defentity users
  (has-many emails {:fk :user_id}))

(defn userid-acceptable? [userid]
  (empty? (select users (where {:userid userid}))))

(defn select-user [userid]
  (-> (select users (where {:userid userid}))
      first))

(defn select-users []
  (select users))

(def ^:private salt (partial str "ohucode/"))

(defn insert-new-user [{email :email
                        userid :userid
                        password :password
                        code :code
                        username :name
                        :as attrs}]
  {:pre [(not-any? nil? [email userid code username password])]}

  (transaction
   (when (zero? (delete signups (where {:email email :userid userid :code code})))
     (throw (RuntimeException. "code does not match")))
   (insert users (values {:userid userid :primary_email email :name username
                          :password_digest
                          (pw/password-digest password (salt userid))}))
   (insert emails (values {:email email :userid userid :verified_at (now)}))))
