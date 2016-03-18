-- 프로젝트 테이블
create table 프로젝트 (
  소유자   varchar(32) not null references 이용자(아이디) on delete cascade,
  이름     varchar(64) not null,
  설명     varchar(255) not null,
  공개     boolean not null default true,
  생성일시 timestamp not null default now(),
  갱신일시 timestamp not null default now(),
  primary key (소유자, 이름)
);

create index 프로젝트_최신순_인덱스 on 프로젝트 (소유자, 이름, 갱신일시 desc);
