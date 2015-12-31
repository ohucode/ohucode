(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:require [ohucode.core :refer :all]
            [ohucode.password :as pw]
            [taoensso.timbre :as timbre]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [clojure.data.json :as json])
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

(defentity audits)

(defn insert-audit [userid action data]
  (insert audits (values {:userid userid :action action
                          :ip (sqlfn "inet" *client-ip*)
                          :data (sqlfn "to_json" (json/write-str data))})))

(defentity signups)

(defn clean-insert-signup [email userid code]
  (let [attrs {:email email :userid userid :code code}]
    (transaction
     (delete signups (where (dissoc attrs :code)))
     (insert signups (values attrs))
     (insert-audit "guest" "reqcode" attrs))))

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
  (select users (order :created_at :DESC)))

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
   (insert users (values {:userid userid :email email :name username
                          :password_digest
                          (pw/ohucode-password-digest userid password)}))
   (insert emails (values {:email email :userid userid :verified_at (now)}))
   (insert-audit userid "signup" {:email email})))

(defn valid-user-password? [userid password]
  (if-let [raw (-> (select-user userid)
                   :password_digest)]
    (let [valid? (pw/ohucode-valid-password? userid password raw)]
      (insert-audit userid "login" {:success valid?})
      valid?)
    (do (insert-audit "guest" "login" {:success false :userid userid})
        false)))
