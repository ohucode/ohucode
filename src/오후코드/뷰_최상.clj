(ns 오후코드.뷰-최상
  (:use [미생.기본]
        [오후코드.기본]
        [오후코드.뷰]))

(함수 basic-content [요청 제목 & 본문]
  (레이아웃 요청 {:title (서비스명+ "> " 제목)}
            [:div.container [:div.row [:h1 제목] 본문]]))

(함수 not-found [요청]
  {:status 404
   :body (basic-content 요청 "찾을 수 없습니다"
                        [:p "요청하신 페이지를 찾을 수 없습니다."])})

(함수 미구현 [요청]
  {:status 404
   :body (basic-content 요청 "아직 구현하지 못한 기능입니다."
                        [:p "요청하신 페이지를 찾을 수 없습니다."])})

(함수 요청에러 [요청 메시지]
  {:status 403
   :body (basic-content 요청 "입력 값 오류"
                        [:p 메시지])})
