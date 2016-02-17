# 오후코드

## 준비

### 1. JDK8 설치
### 2. leiningen 설치 (클로저 대표 빌드툴)

``` sh
$ brew install leiningen
```

### 3. rlwrap 설치 (자바 CLI 애플리케이션에서 readline 처리해주는 툴, 안해도 됨)

``` sh
$ brew install rlwrap
```

### 4. postgresapp 설치 (맥용 PostgreSQL 앱) & 실행

    http://postgresapp.com

### 5. 오후코드 프로젝트 클론

``` sh
$ git clone https://github.com/ohucode/ohucode
```

### 6. 미생 (클로저 한글 확장) 클론

``` sh
$ cd ohucode
$ mkdir checkouts
$ git clone https://github.com/hatemogi/misaeng checkouts/misaeng
```

### 7. DB 유저, 데이터베이스 생성. JDBC 연결설정파일 준비

> 패스워드는 별도 공유합니다.

``` sh
$ openssl enc -d -aes256 -in privates.tar.enc -out privates.tar
$ tar xvf privates.tar
$ cat credentials
```

맥 메뉴바의 postgress.app의 psql을 실행한뒤, ```credentials```에 있는 내용대로 실행.

``` sh
$ cat conf/db_dev.edn
```

위 명령어로, JDBC 연결용 설정파일도 있는지 확인.

### 8. 웹서버 실행 (10000번 포트에 웹서버, 7888포트에 REPL서버가 뜹니다)

```
$ lein run
```

아니면, 아래와 같이 터미널에서 REPL을 따로 띄우거나, Cursive등에서 ```lein repl```로 띄워도 됩니다.

``` sh
$ rlwrap lein repl
```

### 9. 클로저스크립트 빌드툴 (figwheel 실행)

``` sh
$ rlwrap lein figwheel
> (start-autobuild)
```

### 10. 웹브라우저로 접속
    http://0.0.0.0:10000/

### 11. 해피해킹!



### Uploading Data


* ssh

```
$ ssh -x git@github.com "git-receive-pack 'ohucode/ohucode.git'"
00adf32d7b023a47cb783b8417f5cef0e97d62cff4f8 refs/heads/masterreport-status delete-refs side-band-64k quiet atomic ofs-delta agent=git/2:2.4.8~vmg-tokens-sse-1165-ge4f6d1a
0000
```

* https

```
$ curl -i -u ohucode:password 'https://github.com/ohucode/ohucode.git/info/refs?service=git-receive-pack'
HTTP/1.1 200 OK
Server: GitHub Babel 2.0
Content-Type: application/x-git-receive-pack-advertisement
Transfer-Encoding: chunked
Expires: Fri, 01 Jan 1980 00:00:00 GMT
Pragma: no-cache
Cache-Control: no-cache, max-age=0, must-revalidate
Vary: Accept-Encoding

001f# service=git-receive-pack
000000adf32d7b023a47cb783b8417f5cef0e97d62cff4f8 refs/heads/masterreport-status delete-refs side-band-64k quiet atomic ofs-delta agent=git/2:2.4.8~vmg-tokens-sse-1165-ge4f6d1a
```


접속이 끊기지는 않는다.



## 라이선스

Copyright (c) 2016 오후코드
