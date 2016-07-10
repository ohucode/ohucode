(ns 오후코드.핸들러-프로젝트
  (:require [compojure.core :refer :all]
            [오후코드
             [권한 :as 권한]
             [뷰 :as 뷰]
             [저장소 :as 저장소]
             [핸들러-유틸 :refer :all]]))

(defn 프로젝트-미들웨어
  [핸들러 이름공간 플젝명]
  (-> 핸들러
      저장소/저장소읽는-미들웨어
      (권한/플젝읽는-미들웨어 이름공간 플젝명)))

(defroutes 프로젝트-라우트
  (context "/:이름공간/:플젝명" [이름공간 플젝명]
    (미들웨어-라우트 (프로젝트-미들웨어 이름공간 플젝명)
                     (GET "/" 요청
                       (let [프로젝트 (get-in 요청 [:앱 :프로젝트])
                             저장소 (get-in 요청 [:앱 :저장소])]
                         (기본응답 {:프로젝트 프로젝트
                                    :브랜치목록 (저장소/브랜치목록 저장소)
                                    :빈저장소? (저장소/빈저장소? 저장소)
                                    :커밋이력 (저장소/커밋이력 저장소)
                                    :트리 []})))
                     (GET "/commits" [] 뷰/미구현)
                     (GET "/commits/:ref" [ref] 뷰/미구현)
                     (GET "/commit/:commit-id" [commit-id] 뷰/미구현)
                     (GET "/settings" [] 뷰/미구현)
                     (GET "/tree/:ref/:path" [ref path] 뷰/미구현)
                     (GET "/blob/:ref/:path" [ref path] 뷰/미구현)
                     (GET "/tags" [] 뷰/미구현)
                     (GET "/branches" [] 뷰/미구현)
                     (GET "/issues" [] 뷰/미구현))))
