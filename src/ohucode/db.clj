(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:require [taoensso.timbre :as timbre]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [ohucode.password :as password]))

(def ^:private read-edn
  (comp eval read-string slurp))

(defdb dev-db
  (read-edn "conf/db_dev.edn"))

(comment defdb test-db
  (read-edn "conf/db_test.edn"))

(defn now []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defentity signups)

(defn insert-signup [userid email]
  (insert signups (values {:user_id userid :email email
                           :created_at (now)})))

(defentity emails)

(defentity users
  (has-many emails {:fk :user_id}))

(defn select-users []
  (select users))
