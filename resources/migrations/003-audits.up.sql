-- 이용자 액션 기록 테이블
CREATE TABLE 기록 (
  id       SERIAL PRIMARY KEY,
  아이디   VARCHAR(32) NOT NULL REFERENCES 이용자 ON DELETE CASCADE,
  행위     VARCHAR(32) NOT NULL,
  데이터   JSON,
  ip       INET NOT NULL DEFAULT '0.0.0.0',
  생성일시 TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX 기록_아이디_인덱스 ON 기록 (아이디, 생성일시 DESC);

CREATE INDEX 기록_행위_인덱스 ON 기록 (행위, 생성일시 DESC);

CREATE INDEX 기록_ip_인덱스 ON 기록 (ip, 아이디);
