(ns user
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as ragtime]
            [미생.기본 :refer :all]
            [오후코드.db :as db]
            [오후코드.서버 :as 서버]))

(함수- config []
  {:datastore  (jdbc/sql-database {:datasource (:datasource @(:pool db/dev-db))})
   :migrations (jdbc/load-resources "migrations")})

(함수 migrate []
  (ragtime/migrate (config)))

(함수 rollback []
  (ragtime/rollback (config)))

(정의 시작! 서버/시작!)
(정의 중단! 서버/중단!)

(함수 stop-system!
  "cider-refresh를 위한 stop 함수"
  [] (중단!))

(함수 start-system!
  "cider-refresh를 위한 start 함수"
  [] (시작!))
