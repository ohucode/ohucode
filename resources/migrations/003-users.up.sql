-- 사용자 기본 정보
CREATE TABLE users (
  userid VARCHAR(32) PRIMARY KEY,
  email VARCHAR(256) NOT NULL UNIQUE,
  name VARCHAR(64),
  password_digest VARCHAR(32),
  agreement_id INT REFERENCES agreements(id) ON DELETE RESTRICT,
  cohort INT DEFAULT 0,
  company VARCHAR(64),
  title VARCHAR(64),
  url VARCHAR(256),
  location VARCHAR(256),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 영문아이디는 대소문자 구분하지 않고 같게 취급하지만, 보여주는 것은 구분합니다.
CREATE UNIQUE INDEX users_lower_userid_idx ON users (lower(userid));

INSERT INTO users (userid, email, name) VALUES
  ('admin',  'admin@ohucode.com',  '관리자'),
  ('system', 'system@ohucode.com', '시스템'),
  ('guest',  'guest@ohucode.com',  '손님');
