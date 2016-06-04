(ns 오후코드.저장소-검사
  (:use [미생.기본]
        [clojure.test]
        [오후코드.기본]
        [오후코드.저장소])
  (:import [org.eclipse.jgit.lib Repository]
           [오후코드.저장소 저장소레코드]))

(함수- rm-rf! [경로]
  (참이면 (.isDirectory 경로)
    (doseq [파일 (.listFiles 경로)]
      (rm-rf! 파일)))
  (.delete 경로))

(deftest 저장소-검사
  (testing "경로 만들기"
    (is (= "저장소/테스트/테스트플젝1" (저장소경로 "테스트" "테스트플젝1")))
    (binding [*저장소위치* "테스트저장소"]
      (is (= "테스트저장소/테스트/테스트플젝2" (저장소경로 "테스트" "테스트플젝2")))))

  (testing "열기"
    (with-open [ㅈ (열기 "테스트" "테스트리포")]
      (is (인스턴스? 저장소레코드 ㅈ))))

  (testing "빈 저장소인지 확인하기"
    (with-open [빈거 (열기 "테스트" "빈저장소")
                쓴거 (열기 "테스트" "테스트리포")]
      (is (instance? 저장소레코드 빈거))
      (is (true? (빈저장소? 빈거)))
      (is (false? (빈저장소? 쓴거)))))

  (testing "빈 저장소 만들기"
    (삭제! "테스트" "프로젝트")
    (with-open [새거 (생성! "테스트" "프로젝트")]
      (is (인스턴스? 저장소레코드 새거))
      (is (빈저장소? 새거))))

  (testing "브랜치 목록"
    (with-open [빈거 (열기 "테스트" "빈저장소")
                쓴거 (열기 "테스트" "테스트리포")]
      (is (= () (사상 :name (브랜치목록 빈거))))
      (is (= ["refs/heads/master"] (사상 :name (브랜치목록 쓴거)))))))
