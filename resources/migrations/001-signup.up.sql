-- 가입 신청 정보
CREATE TABLE signups (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(16) NOT NULL UNIQUE,
  email VARCHAR(256) NOT NULL UNIQUE,
  verifying_code VARCHAR(6),
  verifying_digest VARCHAR(40),
  requested_at TIMESTAMP,
  sent_at TIMESTAMP,
  expires_at TIMESTAMP
);
