(ns 오후코드.핸들러
  (:use [미생.기본]
        [오후코드.기본]
        [compojure.core]
        [ring.util.response])
  (:require [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.logger.timbre :refer [wrap-with-logger]]
            [prone.middleware :refer [wrap-exceptions]]
            [taoensso.timbre :as timbre]
            [clojure.edn :as edn]
            [오후코드.db :as db]
            [오후코드.보안 :as 보안]
            [오후코드.뷰 :as 뷰]
            [오후코드.저장소 :as 저장소]
            [오후코드.권한 :as 권한]
            [오후코드.핸들러-로그인 :refer [자동로그인-미들웨어 로그인-라우트]]
            [오후코드.핸들러-깃 :refer [smart-http-라우트]]
            [오후코드.핸들러-관리 :refer [관리-라우트]]
            [오후코드.핸들러-가입 :refer [가입-라우트]]))

(매크로 미들웨어-라우트
  "컴포저 라우트에 매칭된 경우에만 미들웨어를 적용하는 매크로."
  [미들웨어+인수 & 라우트목록]
  `(compojure.core/wrap-routes (compojure.core/routes ~@라우트목록)
                               ~@미들웨어+인수))

(매크로대응 라우트정의 defroutes)

(라우트정의 터전-라우트
  (context "/:터전명" [터전명]
    (미들웨어-라우트 (권한/터전-읽는-미들웨어 터전명)
                     (GET "/" 요청 (str (get-in 요청 [:오후코드 :터전주인])))))
  (context "/:터전명" [터전명]
    (미들웨어-라우트 (권한/터전-쓰는-미들웨어 터전명)
                     (POST "/" [프로젝트명 설명 공개?]
                       (조건
                        (db/프로젝트-열람 터전명 프로젝트명)
                        {:status 409 :body {:실패 "이미 있는 프로젝트명"}}

                        참
                        (만약-가정 [터전 (db/이용자-열람 터전명)]
                          ;; TODO: 권한검사, 프로젝트명 유효성 확인, 중복 검사
                          (db/트랜잭션
                           (db/프로젝트-생성 터전명 프로젝트명 설명 공개?)
                           (가정 [ㅈ (저장소/생성! 터전명 프로젝트명)]
                             {:status 200
                              :body {:저장소 ㅈ}})))))
                     (GET "/settings" [] 뷰/미구현)
                     (GET "/profile" [] 뷰/미구현))))

(라우트정의 프로젝트-라우트
  (context "/:터전명/:플젝명" [터전명 플젝명]
    (미들웨어-라우트 (권한/플젝-읽는-미들웨어 터전명 플젝명)
                     (GET "/" [] (println "프로젝트 루트에 걸렸나?") {:프로젝트 {}
                                                                      :브랜치목록 []
                                                                      :커밋목록 []
                                                                      :트리 []})
                     (GET "/commits" [] 뷰/미구현)
                     (GET "/commits/:ref" [ref] 뷰/미구현)
                     (GET "/commit/:commit-id" [commit-id] 뷰/미구현)
                     (GET "/settings" [] 뷰/미구현)
                     (GET "/tree/:ref/:path" [ref path] 뷰/미구현)
                     (GET "/blob/:ref/:path" [ref path] 뷰/미구현)
                     (GET "/tags" [] 뷰/미구현)
                     (GET "/branches" [] 뷰/미구현)
                     (GET "/issues" [] 뷰/미구현))))

(라우트정의 웹-라우트
  (GET "/" [] 뷰/기본)
  (GET "/throw" [] (예외발생 (RuntimeException. "스택트레이스 실험")))
  (GET "/tos" [] 뷰/기본)
  (GET "/policy" [] 뷰/기본)
  (GET "/credits" [] 뷰/기본)
  가입-라우트
  관리-라우트
  로그인-라우트
  터전-라우트
  프로젝트-라우트)

(함수- 콘텐트타입-미들웨어 [핸들러]
  (fn [요청]
    (만약-가정 [응답 (핸들러 요청)]
      (만약 (find-header 응답 "Content-Type")
        응답
        (content-type 응답 "text/html; charset=utf-8")))))

(함수- ip-바인딩-미들웨어 [핸들러]
  (fn [요청]
    (바인딩 [*클라이언트IP* (:remote-addr 요청)]
      (핸들러 요청))))

(함수 이용자-바인딩-미들웨어 [핸들러]
  (fn [요청]
    (바인딩 [*세션이용자* (세션이용자 요청)]
      (핸들러 요청))))

(함수- edn-파라미터-미들웨어
  "요청 컨텐트 타입이 application/edn이면 요청 본문을 EDN 형태로 읽어서 :params 맵에 추가한다."
  [핸들러]
  (가정 [edn? (fn [요청]
                (re-find #"^application/edn" (get-in 요청 [:headers "content-type"] "")))
         ;; [주의] clojure.core/read-string은 eval이 되므로 쓰지 않는다.
         ;; http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/read
         ;; clojure.edn/read-string은 괜찮습니다.
         edn읽기 (합성 edn/read-string slurp)
         edn병합 (fn [요청]
                   (update 요청 :params 병합 (edn읽기 (:body 요청))))]
    (fn [요청]
      (핸들러 (조건-> 요청
                (edn? 요청) edn병합)))))

(함수- edn-응답-미들웨어
  "응답 본문이 맵이면 EDN 표기로 바꿔 보낸다."
  [핸들러]
  (fn [요청]
    (만약-가정 [{본문 :body :as 응답} (핸들러 요청)]
      (만약 (map? 본문)
        (-> 응답
            (assoc :body (pr-str 본문))
            (content-type "application/edn; charset=utf-8"))
        응답))))

(정의 ^:private edn-미들웨어
  (합성 edn-파라미터-미들웨어 edn-응답-미들웨어))

(한번정의
 ^{:private true
   :doc "리로드해도 세션을 유지하기 위해 메모리 세션 따로 둔다"}
  세션저장소 (memory-store))

(라우트정의 앱라우트
  (route/resources "/js"  {:root "public/js"})
  (route/resources "/css" {:root "public/css"})
  (route/resources "/md"  {:root "public/md"})

  (wrap-routes smart-http-라우트
               wrap-defaults api-defaults)

  (-> 웹-라우트
      wrap-with-logger          ; TODO: 로거 위치 고민 필요
      이용자-바인딩-미들웨어
      ip-바인딩-미들웨어
      콘텐트타입-미들웨어
      자동로그인-미들웨어
      edn-미들웨어
      (wrap-defaults (-> site-defaults
                         ;; static 자원은 앞에서 미리 처리한다
                         (dissoc :static)
                         (dissoc :security) ;; TODO: AJAX CSRF 대응
                         (assoc-in [:session :store] 세션저장소))))

  (rfn _ 뷰/not-found))

(정의 앱-dev
  (-> 앱라우트
      wrap-exceptions
      wrap-reload))
