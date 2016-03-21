(ns 오후코드.저장소-실험
  (:use [미생.기본]
        [미생.실험]
        [오후코드.기본]
        [오후코드.저장소])
  (:import [org.eclipse.jgit.lib Repository]
           [오후코드.저장소 저장소레코드]))

(함수- rm-rf! [경로]
  (when (.isDirectory 경로)
    (doseq [파일 (.listFiles 경로)]
      (rm-rf! 파일)))
  (.delete 경로))

(실험정의 저장소-실험
  (실험 "경로 만들기"
    (확인 (= "저장소/테스트/테스트플젝1" (저장소경로 "테스트" "테스트플젝1")))
    (바인딩 [*저장소위치* "테스트저장소"]
      (확인 (= "테스트저장소/테스트/테스트플젝2" (저장소경로 "테스트" "테스트플젝2")))))

  (실험 "열기"
    (with-open [저장소 (열기 "테스트" "테스트리포")]
      (확인 (인스턴스? 저장소레코드 저장소))))

  (실험 "빈저장소인지 확인하기"
    (with-open [빈거 (열기 "테스트" "빈저장소")
                쓴거 (열기 "테스트" "테스트리포")]
      (확인 (인스턴스? 저장소레코드 빈거))
      (확인 (참? (빈저장소? 빈거)))
      (확인 (거짓? (빈저장소? 쓴거)))))

  (실험 "빈 저장소 만들기"
    (삭제! "테스트" "프로젝트")
    (with-open [새거 (생성! "테스트" "프로젝트")]
      (확인 (인스턴스? 저장소레코드 새거))
      (확인 (빈저장소? 새거)))))
