-- 이메일 리스트 테이블
CREATE TABLE emails (
  email VARCHAR(256) PRIMARY KEY,
  userid VARCHAR(32) NOT NULL REFERENCES users ON DELETE CASCADE,
  flag INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),
  verified_at TIMESTAMP
);

-- 사용자 테이블에 있는 이메일 모두 검증 처리
INSERT INTO emails (email, userid, verified_at)
       SELECT email, userid, now() FROM users;

-- 이메일 확인용 테이블
CREATE TABLE email_verifications (
  email VARCHAR(256) NOT NULL REFERENCES emails ON DELETE CASCADE,
  digest VARCHAR(32) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX email_verifications_digest_idx ON email_verifications (digest);
