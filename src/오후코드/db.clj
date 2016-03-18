(ns 오후코드.db
  (:refer-clojure :exclude [update])
  (:use [오후코드.기본]
        [미생.기본]
        [korma.db]
        [korma.core])
  (:require [오후코드.보안 :as 보안]
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

(declare 이용자메일 프로젝트 기록)

(defentity 이용자
  (pk :아이디)
  (has-many 이용자메일 {:fk :아이디})
  (has-many 프로젝트 {:fk :소유자}))

(defentity 이용자메일
  (pk :이메일))

(defentity 기록)

(defentity 프로젝트
  (pk [:소유자 :이름])
  (belongs-to 이용자 {:fk :소유자}))

(함수 insert-audit [아이디 행위 데이터]
  (insert 기록 (values {:아이디 아이디 :행위 행위
                        :ip (sqlfn "inet" *client-ip*)
                        :데이터 (sqlfn "to_json" (json/write-str 데이터))})))

(함수 select-audits []
  (select 기록 (order :생성일시 :DESC) (limit 100)))



(함수 가용이메일? [이메일]
  (empty? (select 이용자메일 (where {:이메일 이메일}))))

(함수 이메일-등록 [주소]
  )


(함수 가용아이디? [아이디]
  (empty?
   (select 이용자 (where (= (sqlfn lower :아이디)
                            (sqlfn lower 아이디))))))

(함수 select-user [아이디]
  (-> (select 이용자 (where (= (sqlfn lower :아이디) (sqlfn lower 아이디))))
      첫째))

(함수 select-users []
  (select 이용자 (order :생성일시 :DESC)))

(함수 신규가입
  "새로운 사용자 가입. 환영 & 확인 메일도 보냅니다."
  [{:keys [이메일 아이디 성명 비밀번호] :as 레코드}]
  {:pre [(not-any? 공? [이메일 아이디 성명 비밀번호])]}

  (가정 [조건 {:이메일 이메일 :아이디 아이디 :성명 성명
               :비번해쉬 (보안/ohucode-password-digest 아이디 비밀번호)}]
    (transaction
     (insert 이용자 (values 조건))
     (insert 이용자메일 (values (select-keys 조건 [:이메일 :아이디])))
     ;; 이메일 발송은 어디서?
     (insert-audit 아이디 "가입" (select-keys 레코드 [:이메일 :성명])))))

(함수 valid-user-password? [아이디 비밀번호]
  (만약-가정 [raw (-> (select-user 아이디) :비번해쉬)]
    (가정 [valid? (보안/ohucode-valid-password? 아이디 비밀번호 raw)]
      (insert-audit 아이디 "login" {:성공 valid?})
      valid?)
    (작용
      (insert-audit "guest" "login" {:성공 거짓 :아이디 아이디})
      거짓)))

(함수 프로젝트생성 [아이디 이름 설명 공개?]
  {:pre [(not-any? 공? [아이디 이름])]}
  (insert 프로젝트 (values {:소유자 아이디 :이름 이름
                            :설명 설명
                            :공개 (boolean 공개?)})))
