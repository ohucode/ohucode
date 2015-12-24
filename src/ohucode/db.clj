(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:require [taoensso.timbre :as timbre]
            [korma.db :refer :all]
            [korma.core :refer :all])
  (:import [java.sql SQLException]))

(def ^:private read-edn
  (comp eval read-string slurp))

(defdb dev-db
  (read-edn "conf/db_dev.edn"))

(comment defdb test-db
  (read-edn "conf/db_test.edn"))

(defn now []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defentity signups)

(defn insert-or-update-signup [email userid passcode]
  (let [key {:email email :userid userid}]
    (if (empty? (select signups (where key)))
      (insert signups
              (values (assoc key :code passcode)))
      (update signups
              (set-fields {:code passcode})
              (where key))
      )))

(defn signup-passcode [email userid]
  (-> (select signups (where {:email email :userid userid}))
      first :code))

(defentity emails)

(defn email-acceptable? [email]
  (empty? (select emails (where {:email email}))))

(defentity users
  (has-many emails {:fk :user_id}))

(defn userid-acceptable? [userid]
  (empty? (select users (where {:userid userid}))))

(defn select-users []
  (select users))
