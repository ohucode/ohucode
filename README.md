# 오후코드


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
