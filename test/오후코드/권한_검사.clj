(ns 오후코드.권한-검사
  (:use [미생.기본]
        [미생.검사]
        [오후코드.권한])
  (:require [오후코드.db :as db]))

(검사정의 권한-검사
  (가정 [핸들러   (상수함수 {:status 200 :body "정상응답"})
         주인요청 {:session {:이용자 {:아이디 "테스트"}}}
         타인요청 {:session {:이용자 {:아이디 "미생"}}}
         손님요청 {:session nil}]

    (검사 "로그인 필수 미들웨어"
      (가정 [로그인핸들러 (로그인필수-미들웨어 핸들러)]
        (확인* [상태 응답] (= 상태 (:status 응답))
               200 (로그인핸들러 주인요청)
               200 (로그인핸들러 타인요청)
               401 (로그인핸들러 손님요청))))

    (검사 "터전 읽는 미들웨어"
      (가정 [주인장 (atom 공)
             터전핸들러 (fn [요청]
                          (reset! 주인장 (get-in 요청 [:오후코드 :터전주인]))
                          (핸들러))
             있는터전 (터전읽는-미들웨어 터전핸들러 "테스트")
             없는터전 (터전읽는-미들웨어 터전핸들러 "없는터전")]
        (확인 (= 404 (:status (없는터전 손님요청))))
        (확인 (공? @주인장))
        (확인 (= 200 (:status (있는터전 손님요청))))
        (확인 (= "테스트" (:아이디 @주인장)))))

    (검사 "터전 쓰는 미들웨어"
      (가정 [있는터전 (터전쓰는-미들웨어 핸들러 "테스트")
             없는터전 (터전쓰는-미들웨어 핸들러 "없는-터전")]
        (확인* [상태 응답] (= 상태 (:status 응답))
               200 (있는터전 주인요청)
               404 (없는터전 주인요청)
               403 (있는터전 손님요청)
               403 (있는터전 타인요청)
               404 (없는터전 손님요청))))

    (가정 [터전명 "테스트"
           공개플젝명 "빈프로젝트"
           비공개플젝명 "비공개리포"]

      (검사 "플젝에 접근할 수 있는 권한 확인하기"
        (가정 [공개플젝 (db/프로젝트-열람 터전명 공개플젝명)
               비공개플젝 (db/프로젝트-열람 터전명 비공개플젝명)]
          (확인 (참?   (:공개 공개플젝)))
          (확인 (거짓? (:공개 비공개플젝)))
          (확인 (참?   (플젝-읽을수있는-아이디? 터전명 공개플젝 nil)))
          (확인 (참?   (플젝-읽을수있는-아이디? 터전명 비공개플젝 터전명)))
          (확인 (거짓? (플젝-읽을수있는-아이디? 터전명 비공개플젝 nil)))
          (확인 (거짓? (플젝-읽을수있는-아이디? 터전명 비공개플젝 "다른누군가")))))

      (검사 "플젝을 읽는 미들웨어"
        (가정 [공개플젝   (플젝읽는-미들웨어 핸들러 터전명 공개플젝명)
               비공개플젝 (플젝읽는-미들웨어 핸들러 터전명 비공개플젝명)]
          (확인* [상태 응답] (= 상태 (:status 응답))
                 200 (공개플젝 손님요청)
                 200 (공개플젝 주인요청)
                 200 (공개플젝 타인요청)
                 404 (비공개플젝 손님요청)
                 200 (비공개플젝 주인요청)
                 404 (비공개플젝 타인요청))))

      (검사 "플젝에 쓰는 미들웨어"
        (가정 [공개플젝   (플젝쓰는-미들웨어 핸들러 터전명 공개플젝명)
               비공개플젝 (플젝쓰는-미들웨어 핸들러 터전명 비공개플젝명)]
          (확인* [상태 응답] (= 상태 (:status 응답))
                 200 (공개플젝 주인요청)
                 404 (공개플젝 손님요청)
                 404 (공개플젝 타인요청)
                 200 (비공개플젝 주인요청)
                 404 (비공개플젝 손님요청)
                 404 (비공개플젝 타인요청)))))))
