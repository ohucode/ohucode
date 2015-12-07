-- 데이터베이스 생성

CREATE DATABASE ohucode_dev OWNER ohucode_web ENCODING 'UTF8';
CREATE DATABASE ohucode_test OWNER ohucode_web ENCODING 'UTF8';

-- 테이블 생성

CREATE TABLE audits (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(32) REFERENCES users(id) ON DELETE CASCADE,
  ip inet4,
  type VARCHAR(16),
  action VARCHAR(16),
  description TEXT,
  created_at TIMESTAMP
);

CREATE TABLE projects (
  id SERIAL PRIMARY KEY,
  name VARCHAR(128),
  path VARCHAR(256) UNIQUE,
  private BOOL DEFAULT FALSE,
  description TEXT,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
