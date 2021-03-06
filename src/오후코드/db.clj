(ns 오후코드.db
  (:refer-clojure :exclude [update])
  (:require [clojure.data.json :as json]
            [korma
             [core :refer :all]
             [db :refer :all]]
            [미생.기본 :refer :all]
            [오후코드
             [기본 :refer :all]
             [보안 :as 보안]]))

(매크로대응 트랜잭션 korma.db/transaction)

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

(함수 기록-남기기 [아이디 행위 데이터]
  (insert 기록 (values {:아이디 아이디 :행위 행위
                        :ip (sqlfn "inet" *클라이언트IP*)
                        :데이터 (sqlfn "to_json" (json/write-str 데이터))})))

(함수 기록-검색 []
  (select 기록 (order :생성일시 :DESC) (limit 100)))

(함수 가용이메일? [이메일]
  (빈? (select 이용자메일 (where {:이메일 이메일}))))

(함수 이메일-등록 [주소]
  )

(함수 가용아이디? [아이디]
  (빈?
   (select 이용자 (where (= (sqlfn lower :아이디)
                            (sqlfn lower 아이디))))))

(함수 이용자-열람 [아이디]
  (-> (select 이용자 (where (= (sqlfn lower :아이디) (sqlfn lower 아이디))))
      첫째))

(함수 이용자-목록 []
  (select 이용자 (order :생성일시 :DESC)))

(함수 신규가입
  "새로운 사용자 가입. 환영 & 확인 메일도 보냅니다."
  [{:keys [이메일 아이디 성명 비밀번호] :as 레코드}]
  {:pre [(not-any? 공? [이메일 아이디 성명 비밀번호])]}

  (가정 [조건 {:이메일 이메일 :아이디 아이디 :성명 성명
               :비번해쉬 (보안/오후코드-비번해쉬 아이디 비밀번호)}]
    (트랜잭션
     (insert 이용자 (values 조건))
     (insert 이용자메일 (values (select-keys 조건 [:이메일 :아이디])))
     ;; 이메일 발송은 어디서?
     (기록-남기기 아이디 "가입" (select-keys 레코드 [:이메일 :성명])))))

(함수 유효-비밀번호? [아이디 비밀번호]
  (만약-가정 [raw (-> (이용자-열람 아이디) :비번해쉬)]
    (가정 [valid? (보안/오후코드-유효비번? 아이디 비밀번호 raw)]
      (기록-남기기 아이디 "login" {:성공 valid?})
      valid?)
    (작용
      (기록-남기기 "guest" "login" {:성공 거짓 :아이디 아이디})
      거짓)))

(함수 프로젝트-생성 [아이디 프로젝트명 설명 공개?]
  {:pre [(not-any? 공? [아이디 프로젝트명])]}
  (insert 프로젝트 (values {:소유자 아이디 :이름 프로젝트명
                            :설명 설명
                            :공개 (boolean 공개?)})))

(함수 프로젝트-열람 [아이디 프로젝트명]
  (-> (select 프로젝트 (where {:소유자 아이디 :이름 프로젝트명}))
      첫째))


(함수 프로젝트-목록 [아이디]
  (select 프로젝트
          (where {:소유자 아이디})
          (order :갱신일시 :DESC)
          (limit 20)))
