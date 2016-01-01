(ns user
  (:require [clojure.test :refer :all]
            [ohucode.git :as git]
            [ohucode.handler :as h]
            [ohucode.server :as s]
            [ohucode.db :as db]
            [ohucode.password :as pw]
            [ohucode.handler-signup-test]
            [ohucode.handler-test]
            [ohucode.handler-git-test]
            [ohucode.password-test]
            [ohucode.db-test]
            [clojure.repl :refer :all]
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
                       ['ohucode.handler-test
                        'ohucode.handler-git-test
                        'ohucode.handler-signup-test
                        'ohucode.password-test
                        'ohucode.db-test])))

(defn RT [& namespaces]
  (refresh)
  (T))
