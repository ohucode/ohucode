# 로그인 쿠키

* 로그인 한 사용자가 차후에 다시 방문했을 때, 쿠키값을 보고, 인증 처리.
* 유효기간은 며칠? 21일. 재방문시 연장?

## 기본 아이디어

* 민감하지 않은 기본 정보를 평문 EDN으로 담고, 타임스탬프를 포함한 서명을 하자.
* 개인키는 인증서버만 알고 있다.
* 공개키는 어디에 노출돼도 상관없다 -> 제3서버에서도 인증 검증은 가능해 진다.

## 궁금한 것

* 이미 클로저도 지원하는 인증 쿠키 미들웨어가 있지 않을까?

## 노출할 기본정보

인증에 필수인 정보를 기록한다. 자주 사용하지 않는 정보는 쿠키에 담지 말고, 필요할 때 서버에 요청하자. 단, 구글어날리틱스(GA)에 보낼 정보는 기본으로 포함하는 것이 좋겠다.

## RSA 키 생성

```sh
$ openssl genrsa -out auth.rsa 2048
$ openssl pkcs8 -in auth.rsa -inform pem -topk8 -out auth.pk8 -outform der -nocrypt
$ openssl rsa -in auth.rsa -pubout -out auth.pub -outform der
```

Java 표준 라이브러리로 읽기 위해 DER포맷의 PKCS8 파일을 준비해둡니다.

### 인증처리를 위한 필수 정보

* 아이디
* 발급일시
* 만료일시
* 인증서버 서명

``` clojure
{:인증 {:아이디 "애월조단", :발급일시 1458014921, :만료일시 1459829321}
 :서명 "G1+hfnsSlzS2SygMTelPWEQJE9EZB9/A5ynqzL709PmBuKlMIhaymYzbMuX0vnFtET1bVl3QV1mLVAki0z23qLr0kP07G8rc4gbT2FjbD+SEfktuMfwyOA2Y31TYT1TaRARUyN5kLAMjeIb0KPIQNPepA4wMUsiRTDRIiABvRw3YuMzQCASW/etybCc3SXc6xOTtzOFwWU/K0vIdw1tTA8AqY6IaVEtqiRbgSvHRKvy5q6rClbiqgH5hZOumdS94CidJs0OsVi35nC7px6HxA8/CB7q7K7Qi7dG37UPfiq4U+5NCMviKors5syzhlJkkEH0twDOdRLUu2ZwfcMVkxg=="}
```

* ```:인증``` 맵을 ```(pr-str)```로 찍어서 그걸 RSA로 서명하고, 그걸 ```:서명``` 값으로 넣는다.
* 후에 "다른 기기에서 로그인한 것 취소"같은 기능을 넣는다면, 발급일시가, 최종 취소요청 일시보다 이전이면 인증처리를 안하도록 처리하면 된다.

### 인증처리후 넘겨줄 세션 프로필 정보

* 성명
* 코호트
* 요금제
* 이메일인증여부
* 약관동의일시

Q. 기본정보를 쿠키에 별도 보관한다면, 애써 세션을 별도 관리할 필요가 있나? 로그인 프로필 정보 외에 세션에 담을 만한 정보는 무엇이 있나?

A. SPA 구조이니만큼, 세션이 필요 없을 수도 있다. 무언가 필요한 기본 정보를 클라이언트에 한번 내려주면, 그걸 브라우저가 보관하면 된다. 아, 그런데 Refresh가 될 수 있구나. Refresh 해도 살아 남아야하는 정보는 뭐가 있나? 프로필 정보는 필요없다. (쿠키로부터 재생가능하다)
