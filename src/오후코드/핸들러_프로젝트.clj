(ns 오후코드.핸들러-프로젝트
  (:use [미생.기본]
        [오후코드.기본]
        [오후코드.핸들러-유틸]
        [compojure.core])
  (:require [오후코드.db :as db]
            [오후코드.뷰 :as 뷰]
            [오후코드.저장소 :as 저장소]
            [오후코드.권한 :as 권한]))

(라우트정의 프로젝트-라우트
  (context "/:터전명/:플젝명" [터전명 플젝명]
    (미들웨어-라우트 (권한/플젝읽는-미들웨어 터전명 플젝명)
                     (GET "/" []
                       (println "프로젝트 루트에 걸렸나?")
                       (기본응답 {:프로젝트 {}
                                  :브랜치목록 []
                                  :커밋목록 []
                                  :트리 []}))
                     (GET "/commits" [] 뷰/미구현)
                     (GET "/commits/:ref" [ref] 뷰/미구현)
                     (GET "/commit/:commit-id" [commit-id] 뷰/미구현)
                     (GET "/settings" [] 뷰/미구현)
                     (GET "/tree/:ref/:path" [ref path] 뷰/미구현)
                     (GET "/blob/:ref/:path" [ref path] 뷰/미구현)
                     (GET "/tags" [] 뷰/미구현)
                     (GET "/branches" [] 뷰/미구현)
                     (GET "/issues" [] 뷰/미구현))))
