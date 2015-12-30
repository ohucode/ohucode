-- 사용자 기본 정보
CREATE TABLE users (
  userid VARCHAR(32) PRIMARY KEY,
  primary_email VARCHAR(256) NOT NULL UNIQUE,
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
