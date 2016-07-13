-- 프로젝트 테이블
CREATE TABLE 프로젝트 (
  소유자   VARCHAR(32) NOT NULL REFERENCES 이용자(아이디) ON DELETE CASCADE,
  이름     VARCHAR(64) NOT NULL,
  설명     VARCHAR(255),
  공개     BOOLEAN NOT NULL DEFAULT TRUE,
  생성일시 TIMESTAMP NOT NULL DEFAULT now(),
  갱신일시 TIMESTAMP NOT NULL DEFAULT now(),
  PRIMARY KEY (소유자, 이름)
);

CREATE INDEX 프로젝트_최신순_인덱스 ON 프로젝트 (소유자, 이름, 갱신일시 DESC);

INSERT INTO 프로젝트 (소유자, 이름, 설명, 공개) VALUES
  ('test', 'empty',   '빈 프로젝트 테스트용도', TRUE),
  ('test', 'fixture', '각종 테스트용도', TRUE),
  ('test', 'private', '비공개 저장소 테스트용도', FALSE);
