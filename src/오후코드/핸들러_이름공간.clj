(ns 오후코드.핸들러-이름공간
  (:require [compojure.core :refer :all]
            [미생.기본 :refer :all]
            [오후코드
             [db :as db]
             [권한 :as 권한]
             [뷰 :as 뷰]
             [저장소 :as 저장소]
             [핸들러-유틸 :refer :all]]))

(defroutes 이름공간-라우트
  (context "/:ns" [ns]
    (미들웨어-라우트 (권한/공간읽는-미들웨어 ns)
                     (웹요청-라우트
                      (GET "/" 요청 ns))
                     (GET "/" 요청
                       ;; TODO: 이름공간 대소문자 무시하기
                       (기본응답 {:공간주인 (get-in 요청 [:오후코드 :공간주인])
                                  :플젝목록 (db/프로젝트-목록 ns)}))))
  (context "/:ns" [ns]
    (미들웨어-라우트 (권한/공간쓰는-미들웨어 ns)
                     (POST "/" [프로젝트명 설명 공개?]
                       (조건
                        (db/프로젝트-열람 ns 프로젝트명)
                        (기본응답 409 {:실패 "이미 있는 프로젝트명"})

                        참
                        (만약-가정 [공간 (db/이용자-열람 ns)]
                          ;; TODO: 권한검사, 프로젝트명 유효성 확인, 중복 검사
                          (db/트랜잭션
                           (db/프로젝트-생성 ns 프로젝트명 설명 공개?)
                           (가정 [ㅈ (저장소/생성! ns 프로젝트명)]
                             (기본응답 {:저장소 ㅈ}))))))
                     (GET "/settings" [] 뷰/미구현)
                     (GET "/profile" [] 뷰/미구현))))
