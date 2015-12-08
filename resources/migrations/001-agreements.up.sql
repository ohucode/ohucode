-- 이용약관
CREATE TABLE agreements (
  id SERIAL PRIMARY KEY,
  url VARCHAR(256),
  digest VARCHAR(32),
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO agreements (url, digest) VALUES ('to-be-set', 'do-not-mean-a-thing');
