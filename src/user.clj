(ns user
  (:require [clojure.test :refer :all]
            [ohucode.git :as git]
            [ohucode.handler :as h]
            [ohucode.server :as s]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn T []
  (run-tests 'ohucode.handler-test 'ohucode.handler-git-test
             'ohucode.password-test))

(defn RT []
  (refresh)
  (T))
