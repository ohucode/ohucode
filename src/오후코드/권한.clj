(ns 오후코드.권한
  (:require [미생.기본 :refer :all]
            [오후코드
             [db :as db]
             [기본 :refer :all]]))

(defn- 거절하기 [_]
  {:status 403 :body "권한이 없습니다."})

(defn- 없다하기 [_]
  {:status 404 :body "찾을 수 없습니다."})

(함수 로그인필수-미들웨어
  "로그인 처리된 요청만 핸들러에 넘기고, 그렇지 않으면 401응답을 준다.
  검증은 별도로 처리해야 하며, 이건 이미 검증된 로그인 정보를 확인한다."
  ([핸들러]
   (로그인필수-미들웨어 핸들러 (constantly {:status 401 :body "로그인해 주세요."})))
  ([핸들러 거절하기]
   (fn [요청]
     ((만약 (세션이용자 요청) 핸들러 거절하기) 요청))))

(함수 공간읽는-미들웨어
  "요청한 이름공간이 있으면 처리, 없으면 404 내보내기.
  이름공간은 모두에게 (읽기) 공개이므로, 로그인 확인할 필요없다.
  접근한 공간주인 정보를 [요청]맵의 [:오후코드 :공간주인]에 연결한다."
  [핸들러 이름공간]
  (fn [요청]
    (가정 [공간주인 (select-keys (db/이용자-열람 이름공간)
                                 [:아이디 :성명 :생성일시 :거주지역 :url])]
      (만약 (빈? 공간주인)
        (없다하기 요청)
        (핸들러 (assoc-in 요청 [:앱 :공간주인] 공간주인))))))

(함수 공간-쓸수있는-아이디?
  "특정 이름공간에 접근한 이용자가 쓰기 권한이 있는지 확인.
  지금은 주인만 쓸 수 있다."
  [이름공간 아이디]
  (= 이름공간 아이디))

(함수 공간쓰는-미들웨어
  "이름공간 있는지 확인하고, 있다면 권한을 확인한다.
  권한을 확인할 때는 로그인 정보가 필요하다.
  이름공간은 공개된 정보이므로, 쓸 권한이 없다면 403 응답을 준다."
  [핸들러 이름공간]
  (-> (fn [요청]
        (만약 (공간-쓸수있는-아이디? 이름공간 (세션이용자-아이디 요청))
          (핸들러 요청)
          (거절하기 요청)))
      (로그인필수-미들웨어 거절하기)
      (공간읽는-미들웨어 이름공간)))

(함수 플젝-읽을수있는-아이디?
  "특정 플젝에 접근한 이용자가 읽기 권한이 있는지 확인.
  공개플젝이면 아무나 읽을 수 있고, 비공개면 주인만 읽을 수 있다."
  [이름공간 플젝 아이디]
  (or (:공개 플젝) (= 이름공간 아이디)))

(함수 플젝-쓸수있는-아이디?
  "특정 플젝에 접근한 이용자가 쓰기 권한이 있는지 확인.
  지금은 공간주인만 쓸 수 있다."
  [이름공간 플젝 아이디]
  (= 이름공간 아이디))

(defn 플젝읽는-미들웨어
  [핸들러 이름공간 플젝명]
  (-> (fn [요청]
        (let [플젝 (db/프로젝트-열람 이름공간 플젝명)]
          (if (플젝-읽을수있는-아이디? 이름공간 플젝 (세션이용자-아이디 요청))
            (핸들러 (assoc-in 요청 [:앱 :프로젝트] 플젝))
            (없다하기 요청))))
      (공간읽는-미들웨어 이름공간)))

(함수 플젝쓰는-미들웨어
  [핸들러 이름공간 플젝명]
  (-> (fn [요청]
        (가정 [플젝 (get-in 요청 [:앱 :프로젝트])]
          (만약 (플젝-쓸수있는-아이디? 이름공간 플젝 (세션이용자-아이디 요청))
            (핸들러 요청)
            (없다하기 요청))))
      (플젝읽는-미들웨어 이름공간 플젝명)))
