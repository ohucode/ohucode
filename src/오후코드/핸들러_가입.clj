(ns 오후코드.핸들러-가입
  (:require [compojure.core :refer :all]
            [미생.기본 :refer :all]
            [오후코드.db :as db]))

(정의 금지아이디
  #{"admin" "js" "css" "static" "fonts" "signup" "login" "logout"
    "settings" "help" "support" "notifications" "notification"
    "status" "components" "news" "account" "templates"
    "tos" "policy" "test" "ohucode" "root" "system"
    "credits" "user" "md"
    "관리자" "운영자" "테스트" "마스터" "루트" "어드민" "도움말" "가입"
    "설정" "알림" "쪽지" "이슈" "계정" "로그인" "정책" "약관" "시스템"})

(함수 가용아이디? [아이디]
  (and 아이디
       (re-matches #"[가-힣a-z\d][가-힣a-z\d_\-]{3,15}" 아이디)
       (부정 (금지아이디 아이디))
       (db/가용아이디? 아이디)))

(함수 가용이메일? [이메일]
  ;; TODO: 이메일 포맷 검증 어찌할까?
  (and 이메일
       (re-matches #".+\@.+\..+" 이메일)
       (db/가용이메일? 이메일)))

(함수 가용비밀번호? [비밀번호]
  ;; TODO: 조금 더 까다롭게
  (and 비밀번호
       (<= 7 (개수 비밀번호))))

(정의 가입-라우트
  (context "/signup" []
    (POST "/" [이메일 아이디 비밀번호 성명 :as 요청]
      (pr ["POST" 요청])
      (만약 (and (가용이메일? 이메일)
                 (가용아이디? 아이디)
                 (가용비밀번호? 비밀번호))
        (작용
          (db/신규가입 (select-keys (:params 요청)
                                    [:이메일 :아이디 :비밀번호 :성명]))
          {:status 200 :body {:성공 "가입 신청 완료"}})
        {:status 409 :body {:실패 "아이디나 이메일을 사용할 수 없습니다"}}))))
