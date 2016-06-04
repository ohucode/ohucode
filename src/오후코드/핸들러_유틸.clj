(ns 오후코드.핸들러-유틸
  (:require [미생.기본 :refer :all]))

(매크로대응 라우트정의 compojure.core/defroutes)

(매크로 미들웨어-라우트
  "컴포저 라우트에 매칭된 경우에만 미들웨어를 적용하는 매크로."
  [미들웨어+인수 & 라우트목록]
  `(compojure.core/wrap-routes (compojure.core/routes ~@라우트목록)
                               ~@미들웨어+인수))

(함수 edn요청?
  "요청 콘텐트타입이 edn인지 확인한다."
  [요청]
  (re-find #"^application/edn"
           (get-in 요청 [:headers "content-type"]
                   (get-in 요청 [:headers "accept"] ""))))

(함수 요청선택-미들웨어 [핸들러 선택함수]
  (fn [요청]
    (만약 (선택함수 요청) (핸들러 요청))))

(매크로 웹요청-라우트
  "edn요청이 아닌 경우에만 처리하는 라우트"
  [& 라우트목록]
  `(미들웨어-라우트 (요청선택-미들웨어 (complement edn요청?))
                    ~@라우트목록))

(매크로 API요청-라우트
  "edn요청인 경우에만 처리하는 라우트"
  [& 라우트목록]
  `(미들웨어-라우트 (요청선택-미들웨어 edn요청?)
                    ~@라우트목록))

(함수 기본응답
  "기본 응답 맵 만들기\n
  {:status 상태 :body 본문}"
  ([본문] (기본응답 200 본문))
  ([상태 본문] {:status 상태 :body 본문}))
