-- 사용자 액션 기록 테이블
CREATE TABLE audits (
  id         SERIAL PRIMARY KEY,
  userid     VARCHAR(32) NOT NULL REFERENCES users ON DELETE CASCADE,
  action     VARCHAR(32) NOT NULL,
  data       JSON,
  ip         INET NOT NULL DEFAULT '0.0.0.0',
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX audits_idx_by_userid ON audits (userid, created_at DESC);

CREATE INDEX audits_idx_by_action ON audits (action, created_at DESC);

CREATE INDEX audits_idx_by_ip ON audits (ip, userid);
