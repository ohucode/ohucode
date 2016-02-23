# EDN

> 클로저에서 쓰는 자료형 표기법. JSON을 쓸만한 곳에 EDN을 써도 된다.

``` clojure
(clojure.edn/read-string "{:userid \"hatemogi\" :verified true}")
; => {:userid "hatemogi" :verified true}

(clojure.core/pr-str {:userid "hatemogi" :verified true})
; => "{:userid \"hatemogi\" :verified true}"
```

* 클로저와 클로저스크립트 모두 기본 포함됨.
