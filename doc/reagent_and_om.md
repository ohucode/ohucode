# 클로저스크립트용 React 라이브러리 개요

* Om : 가장 많이 쓰이는듯.
* Reagent : 가장 간단하게 구현함. 특화된 atom으로 상태관리.
* Rum : 간단한데 약간의 스펙이 있어서, React 라이프사이클에 유용.

Om은 클로저 프로토콜을 사용하고, 기본은 Hiccup 스타일이 아니라서, 별도 라이브러리를 추가해서 사용함. Reagent와 Rum은 둘 다 hiccup 스타일을 쓴다.


## Reagent

### 컴포넌트

* 일반 클로저 함수를 써서 react의 컴포넌트를 표현한다.

```clojure
(defn 컴포넌트 [속성]
  [:div.panel
    [:div.panel-header "제목"]
    [:div.panel-body [:div "패널 내용"]]])
```

* 함수를 돌려주는 함수(Form-2)를 쓰면, 컴포넌트 로컬 상태를 관리할 수 있다.
* 참고: https://github.com/Day8/re-frame/wiki/Creating-Reagent-Components

```clojure
(defn 로컬상태-있는-컴포넌트 [속성]
  (let [로컬상태 (r/atom {})]
    (fn [속성] ; 바깥의 함수와 동일한 파라미터를 받는다.
      [:div "아주 멋진 hiccup-style 벡터 표현])))
```

* 이렇게 하면, React의 ```:component-did-mount```등의 라이프사이클 관리가 대폭 편리해진다. (대부분 로컬상태 처리만으로 원하는 일을 할 수 있다)

### ratom
* reagent 특화 처리한 ```reagent.core/atom``` (이하 r/atom)을 쓴다.
* ```clojure.core/atom```과 동일하게 쓸 수 있는데, reagent의 뷰에서 참조하고 있으면, 변경시 자동으로 뷰가 업데이트된다.
* ```reagent.ratom/reaction``` 매크로를 쓰면, 원본 atom의 변화에 따라서 영향받는 ```r/atom```을 쉽게 만들 수 있다. ```add-watch```를 써서 또 다른 ```r/atom```을 관리하는 것과 같은 효과이지만 편리하다.


## re-frame (Reagent로 SPA 만들 때 유용)

* https://github.com/Day8/re-frame
* 이벤트 기준으로 처리하며, 애플리케이션 상태는 직접 건드리지 않는 방식.
* 이벤트 핸들러를 등록해서 애플리케이션 상태가 전이된다.
* 애플리케이션 상태에 따른 별도의 뷰를 관리하고, 그 뷰를 구독해서 필요에 따른 형태로 렌더링.


### 2016/03/05

* re-frame으로 input 필드를 관리하자니, 한글 입력시 꼬이는 현상이 있다. 해결하는데 너무 많은 시간을 쏟는 것 같아서, 일단 원래의 기본 r/atom 방식으로 input 값을 바꾸고, 최종 서브밋 시점에서만 이벤트로 처리해보자.
