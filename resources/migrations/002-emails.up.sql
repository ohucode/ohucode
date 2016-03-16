-- 이메일 리스트 테이블
create table 이용자메일 (
  이메일   varchar(256) primary key,
  아이디   varchar(32) not null references 이용자 on delete cascade,
  플래그   int not null default 0,
  생성일시 timestamp not null default now(),
  갱신일시 timestamp not null default now(),
  확인일시 timestamp
);

-- 이용자 테이블에 있는 이메일 모두 검증 처리
insert into 이용자메일 (이메일, 아이디, 확인일시)
       select 이메일, 아이디, now() from 이용자;

-- 이메일 확인용 테이블
create table 이용자메일확인 (
  이메일   varchar(256) not null references 이용자메일 on delete cascade,
  확인해쉬 varchar(32) not null,
  생성일시 timestamp not null default now()
);

create unique index 이용자메일확인_해쉬_인덱스 on 이용자메일확인 (확인해쉬);
