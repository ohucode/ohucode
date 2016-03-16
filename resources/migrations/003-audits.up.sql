-- 이용자 액션 기록 테이블
create table 기록 (
  id       serial primary key,
  아이디   varchar(32) not null references 이용자 on delete cascade,
  행위     varchar(32) not null,
  데이터   json,
  ip       inet not null default '0.0.0.0',
  생성일시 timestamp not null default now()
);

create index 기록_아이디_인덱스 on 기록 (아이디, 생성일시 desc);

create index 기록_행위_인덱스 on 기록 (행위, 생성일시 desc);

create index 기록_ip_인덱스 on 기록 (ip, 아이디);
