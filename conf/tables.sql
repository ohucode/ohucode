CREATE DATABASE ohucode_dev OWNER ohucode_web ENCODING 'UTF8';

DROP TABLE signup_requests, audits, emails, users;

CREATE TABLE signup_requests (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(16) NOT NULL UNIQUE,
  email VARCHAR(256) NOT NULL UNIQUE,
  verifying_code VARCHAR(6),
  verifying_digest VARCHAR(40),
  requested_at TIMESTAMP,
  sent_at TIMESTAMP,
  expires_at TIMESTAMP
);

CREATE TABLE users (
  seq SERIAL,
  id VARCHAR(16) PRIMARY KEY,
  name VARCHAR(32),
  primary_email VARCHAR(256) NOT NULL UNIQUE,
  password VARCHAR(64),
  company VARCHAR(256),
  url VARCHAR(256),
  location VARCHAR(256),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

INSERT INTO users (id, name, location) VALUES
  ('hatemogi', '김대현', '제주'),
  ('test', '테스트', '작업실');

CREATE TABLE emails (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(32) REFERENCES users(id) ON DELETE CASCADE,
  email VARCHAR(256) NOT NULL UNIQUE,
  verifying_key CHAR(40) UNIQUE,
  verifying_mail_sent_at TIMESTAMP,
  verified_at TIMESTAMP
);

INSERT INTO emails (user_id, email) VALUES
  ('hatemogi', 'hatemogi@gmail.com'),
  ('hatemogi', 'dhk@ohucode.com'),
  ('test', 'hatemogi+test@gmail.com');

CREATE TABLE audits (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(32) REFERENCES users(id) ON DELETE CASCADE,
  action VARCHAR(8),
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
