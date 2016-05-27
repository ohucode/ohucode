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

(함수 도움말 []
  (println
   "> (시작!)    ; => 웹서버/레플서버 띄우기
> (중단!)    ; => 웹서버/레플서버 중단하기
> (migrate)  ; => DB 스키마 마이그레션 하기
> (rollback) ; => DB 스키마 한단계 롤백 하기
> (도움말)   ; => 이 설명 보이기"))

(정의 help 도움말)
