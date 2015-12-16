-- 가입 신청 정보
CREATE TABLE signups (
  email VARCHAR(256),
  userid VARCHAR(32),
  code VARCHAR(8) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  PRIMARY KEY (email, userid)
);

CREATE INDEX signups_idx ON signups (created_at DESC);
