(ns user
  (:use [미생.기본]
        [미생.검사]
        [clojure.test]
        [clojure.repl])
  (:require [오후코드.git :as git]
            [오후코드.핸들러 :as h]
            [오후코드.서버 :as s]
            [오후코드.db :as db]
            [오후코드.보안 :as pw]
            [clojure.tools.namespace.repl :refer [refresh]]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as ragtime]))

(defn- config []
  {:datastore  (jdbc/sql-database {:datasource (:datasource @(:pool db/dev-db))})
   :migrations (jdbc/load-resources "migrations")})

(defn migrate []
  (ragtime/migrate (config)))

(defn rollback []
  (ragtime/rollback (config)))
