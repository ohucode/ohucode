# EDN

> 클로저에서 쓰는 자료형 표기법. JSON을 쓸만한 곳에 EDN을 써도 된다.

``` clojure
(clojure.edn/read-string "{:userid \"hatemogi\" :verified true}")
; => {:userid "hatemogi" :verified true}

(clojure.core/pr-str {:userid "hatemogi" :verified true})
; => "{:userid \"hatemogi\" :verified true}"

(cljs.tools.reader.edn/read-string "{}")
; => {}
```

* 클로저와 클로저스크립트 둘다 기본으로 처리 가능

## 응답 본문이 map일때 EDN 응답으로 처리

* Content-Type: application/edn

## 참고할 라이브러리

* https://github.com/ngrunwald/ring-middleware-format
* https://github.com/tailrecursion/ring-edn
