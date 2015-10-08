(ns ohucode.db
  (:refer-clojure :exclude [update])
  (:require [taoensso.timbre :as timbre]
            [korma.db :refer :all]
            [korma.core :refer :all]))

(defdb dev
  ((comp eval read-string slurp) "conf/db_dev.edn"))

(defentity emails)

(defentity users
  (has-many emails {:fk :user_id}))

(prn (select users (with emails)))
