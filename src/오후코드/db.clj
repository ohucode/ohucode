(ns 오후코드.db
  (:refer-clojure :exclude [update])
  (:use [오후코드.기본]
        [미생.기본]
        [korma.db]
        [korma.core])
  (:require [오후코드.password :as pw]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json]
            [clojure.set :refer [rename-keys]])
  (:import [java.sql SQLException]))

(정의 ^:private read-edn
  (comp eval read-string slurp))

(defdb dev-db
  (read-edn "conf/db_dev.edn"))

(함수 now
  "현재 시각 in java.sql.Timestamp.
  현재 시각에서 [dsec]초 이후 시간."
  ([] (now 0))
  ([dsec] (java.sql.Timestamp. (+ (.getTime (java.util.Date.))
                                  (* 1000 dsec)))))

(defentity audits)

(함수 insert-audit [아이디 행동 데이터]
  (insert audits (values {:userid 아이디 :action 행동
                          :ip (sqlfn "inet" *client-ip*)
                          :data (sqlfn "to_json" (json/write-str 데이터))})))

(함수 select-audits []
  (select audits (order :created_at :DESC) (limit 100)))

(defentity emails)

(함수 가용이메일? [이메일]
  (empty? (select emails (where {:email 이메일}))))

(함수 이메일-등록 [주소]
  )

(defentity users
  (has-many emails {:fk :userid}))

(함수 가용아이디? [아이디]
  (empty?
   (select users (where (= (sqlfn lower :userid)
                           (sqlfn lower 아이디))))))

(함수 select-user [아이디]
  (-> (select users (where (= (sqlfn lower :userid) (sqlfn lower 아이디))))
      첫째
      (rename-keys {:userid   :아이디
                    :name     :성명
                    :cohort   :집단
                    :company  :소속
                    :location :거주})))

(함수 select-users []
  (select users (order :created_at :DESC)))

(함수 신규가입
  "새로운 사용자 가입. 환영 & 확인 메일도 보냅니다."
  [{:keys [이메일 아이디 성명 비밀번호] :as 레코드}]
  {:pre [(not-any? 공? [이메일 아이디 성명 비밀번호])]}

  (가정 [조건 {:email 이메일 :userid 아이디 :name 성명
               :password_digest (pw/ohucode-password-digest 아이디 비밀번호)}]
    (transaction
     (insert users (values 조건))
     (insert emails (values (select-keys 조건 [:email :userid])))
     ;; 이메일 발송은 어디서?
     (insert-audit 아이디 "가입" (select-keys 레코드 [:이메일 :성명])))))

(함수 valid-user-password? [아이디 비밀번호]
  (만약-가정 [raw (-> (select-user 아이디)
                      :password_digest)]
    (가정 [valid? (pw/ohucode-valid-password? 아이디 비밀번호 raw)]
      (insert-audit 아이디 "login" {:성공 valid?})
      valid?)
    (작용
      (insert-audit "guest" "login" {:성공 거짓 :아이디 아이디})
      거짓)))
