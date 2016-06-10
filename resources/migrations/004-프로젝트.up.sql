-- 프로젝트 테이블
create table 프로젝트 (
  소유자   varchar(32) not null references 이용자(아이디) on delete cascade,
  이름     varchar(64) not null,
  설명     varchar(255),
  공개     boolean not null default true,
  생성일시 timestamp not null default now(),
  갱신일시 timestamp not null default now(),
  primary key (소유자, 이름)
);

create index 프로젝트_최신순_인덱스 on 프로젝트 (소유자, 이름, 갱신일시 desc);

insert into 프로젝트 (소유자, 이름, 설명, 공개) values
  ('테스트', '빈프로젝트', '빈 프로젝트 테스트용도', true),
  ('테스트', '테스트리포', '각종 테스트용도', true),
  ('테스트', '비공개리포', '비공개 저장소 테스트용도', false);
