-- 이메일 리스트 테이블
CREATE TABLE emails (
  email VARCHAR(256) PRIMARY KEY,
  userid VARCHAR(32) NOT NULL REFERENCES users ON DELETE CASCADE,
  flag  INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  verified_at TIMESTAMP
);

INSERT INTO emails (email, userid, verified_at) VALUES
  ('admin@ohucode.com', 'admin', now()),
  ('system@ohucode.com', 'system', now()),
  ('guest@ohucode.com', 'guest', now());

-- 이메일 확인용 테이블
CREATE TABLE email_verifications (
  email VARCHAR(256) NOT NULL REFERENCES emails ON DELETE CASCADE,
  digest VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);
