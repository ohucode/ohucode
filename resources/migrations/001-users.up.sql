-- 이용자 기본 정보
CREATE TABLE 이용자 (
  아이디   VARCHAR(32) PRIMARY KEY,
  이메일   VARCHAR(256) NOT NULL UNIQUE,
  닉네임   VARCHAR(64) UNIQUE,
  성명     VARCHAR(64),
  비번해쉬 VARCHAR(32),

  코호트   INT DEFAULT 0,
  요금제   INT DEFAULT 0,
  소속     VARCHAR(64),
  거주지역 VARCHAR(64),
  URL      VARCHAR(256),
  생성일시 TIMESTAMP NOT NULL DEFAULT now(),
  갱신일시 TIMESTAMP NOT NULL DEFAULT now(),
  동의일시 TIMESTAMP NOT NULL DEFAULT now()
);

-- 영문아이디는 대소문자 구분하지 않고 같게 취급하지만, 보여주는 것은 구분합니다.
CREATE UNIQUE INDEX 이용자_소문자_아이디_인덱스 ON 이용자 (lower(아이디));

INSERT INTO 이용자 (아이디, 이메일, 성명, 비번해쉬) VALUES
  ('admin',    'admin@ohucode.com',  '관리자', NULL),
  ('system',   'system@ohucode.com', '시스템', NULL),
  ('guest',    'guest@ohucode.com',  '손님',   NULL),
  ('test',     'test@ohucode.com',   '오테슽', 'vsrusjmRxBqVM+wLYA5TP5OQJVk='),
  ('awjordan', 'dhk@ohucode.com',    '김대현', 'JGnGP3MXoUsAkRaRFHyTMOtDIqU=');

-- test / test1234
-- awjordan / jordan1234
