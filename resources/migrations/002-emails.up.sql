-- 이메일 리스트 테이블
CREATE TABLE 이용자메일 (
  이메일   VARCHAR(256) PRIMARY KEY,
  아이디   VARCHAR(32) NOT NULL REFERENCES 이용자 ON DELETE CASCADE,
  플래그   INT NOT NULL DEFAULT 0,
  생성일시 TIMESTAMP NOT NULL DEFAULT now(),
  갱신일시 TIMESTAMP NOT NULL DEFAULT now(),
  확인일시 TIMESTAMP
);

-- 이용자 테이블에 있는 이메일 모두 검증 처리
INSERT INTO 이용자메일 (이메일, 아이디, 확인일시)
       SELECT 이메일, 아이디, now() FROM 이용자;

-- 이메일 확인용 테이블
CREATE TABLE 이용자메일확인 (
  이메일   VARCHAR(256) NOT NULL REFERENCES 이용자메일 ON DELETE CASCADE,
  확인해쉬 VARCHAR(32) NOT NULL,
  생성일시 TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX 이용자메일확인_해쉬_인덱스 ON 이용자메일확인 (확인해쉬);
