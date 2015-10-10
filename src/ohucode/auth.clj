(ns ohucode.auth
  (:require [compojure.core :refer :all]
            [ring.util.response :refer :all]
            [ring.middleware.session :as s]
            [ohucode.db :as db]))

(defn auth? [req]
  true)

(defn user-info [req]
  nil)

(defn login [req username password]
  )

(defn logout [req])

