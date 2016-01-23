(ns user
  (:use [미생.기본]
        [미생.실험]
        [clojure.test]
        [clojure.repl])
  (:require [오후코드.git :as git]
            [오후코드.핸들러 :as h]
            [오후코드.서버 :as s]
            [오후코드.db :as db]
            [오후코드.password :as pw]
            [오후코드.핸들러-가입-실험]
            [오후코드.핸들러-실험]
            [오후코드.핸들러-깃-실험]
            [오후코드.password-실험]
            [오후코드.db-실험]
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

(defn T [& namespaces]
  (apply run-tests (or namespaces
                       ['오후코드.핸들러-실험
                        '오후코드.핸들러-깃-실험
                        '오후코드.핸들러-가입-실험
                        '오후코드.password-실험
                        '오후코드.db-실험])))

(defn RT [& namespaces]
  (refresh)
  (T))
