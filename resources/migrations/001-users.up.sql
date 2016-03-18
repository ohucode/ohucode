-- 이용자 기본 정보
create table 이용자 (
  아이디   varchar(32) primary key,
  이메일   varchar(256) not null unique,
  성명     varchar(64),
  비번해쉬 varchar(32),

  코호트   int default 0,
  요금제   int default 0,
  소속     varchar(64),
  거주지역 varchar(64),
  url      varchar(256),
  생성일시 timestamp not null default now(),
  갱신일시 timestamp not null default now(),
  동의일시 timestamp not null default now()
);

-- 영문아이디는 대소문자 구분하지 않고 같게 취급하지만, 보여주는 것은 구분합니다.
create unique index 이용자_소문자_아이디_인덱스 on 이용자 (lower(아이디));

insert into 이용자 (아이디, 이메일, 성명, 비번해쉬) values
  ('admin',    'admin@ohucode.com',   '관리자', null),
  ('system',   'system@ohucode.com',  '시스템', null),
  ('guest',    'guest@ohucode.com',   '손님',   null),
  ('테스트',   'test@ohucode.com',    '오테슽', 'lqkKsfp7wFBANWfbvtcqG2QDNh8='),
  ('미생',     'misaeng@ohucode.com', '장그래', null),
  ('애월조단', 'dhk@ohucode.com',     '김대현', 'kkUDtH2Ee/iZ6ldzrNXpzNKHdJk=');
