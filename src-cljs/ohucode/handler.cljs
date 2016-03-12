(ns ohucode.handler
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST PUT 검증반응]]))

(def ^{:doc "애플리케이션 상태 초기화"}
  초기상태 {:가입신청 {}             ; 가입신청시 신청 정보 임시 보관
            :가입결과 {}             ; 가입신청 결과 성패와 설명
            :이용자 {}               ; 로그인한 이용자 정보
            :페이지 {}               ; <main></main> 화면에 보여줄 페이지
            :미리보기 false          ; 개발용 미리보기 모드 on/off
            :새프로젝트 {}           ; 새프로젝트 신청/결과
            })

(def ^:private 핸들러 register-handler)

(핸들러 :가입폼
        (fn [db [_ 키 값]]
          #_(js/console.log "가입폼" 값)
          (assoc-in db [:가입폼 키] 값)))

(핸들러 :가입신청
        (fn [db [_ 이용자]]
          (js/console.log "가입신청" (str 이용자))
          (POST "/signup"
              {:내용 이용자
               :성공 (fn [내용] (dispatch [:가입결과 :성공 내용]))
               :실패 (fn [코드 내용] (dispatch [:가입결과 :실패 내용]))})
          (assoc db :가입신청 {:로딩? true
                               :이용자 이용자
                               :결과 {}})))

(핸들러 :가입결과
        (fn [db [_ 성패 내용]]
          (js/console.log "가입결과" (name 성패))
          (cond-> (assoc-in db [:가입신청 :로딩?] false)
            (= :실패 성패) (assoc-in [:가입신청 :실패] "실패했어요"))))

(핸들러 :로그인
        (fn [db [_ 이용자]]
          (js/console.log #js ["로그인 요청" 이용자])
          (POST "/user/login"
              {:내용 이용자
               :성공 #(dispatch [:로그인결과 :성공 %])
               :실패 (fn [코드 내용] (dispatch [:로그인결과 :실패 내용]))})
          (assoc db :로그인 {:로딩? true :결과 {}})))

(핸들러 :로그인결과
        (fn [db [_ 성패 내용]]
          (js/console.log "로그인결과" (name 성패) (str 내용))
          (dispatch [:페이지 :이용자홈])
          (cond-> (assoc-in db [:로그인 :로딩?] false)
            (= :성공 성패) (assoc-in [:로그인 :이용자] (:이용자 내용)))))

(핸들러 :로그아웃
        (fn [db _]
          (PUT "/user/logout"
              {:내용 {}
               :성공 #(dispatch [:로그아웃결과 :성공])
               :실패 #(dispatch [:로그아웃결과 :실패])})
          db))

(핸들러 :로그아웃결과
        (fn [db [_ 성패]]
          (js/console.log "로그아웃결과" (name 성패))
          (if (= :성공 성패)
            (do
              (dispatch [:페이지 :이용자홈])
              (dissoc db :로그인))
            (do
              (dispatch [:페이지 :첫페이지>가입])
              (assoc db :알림 "실패")))))

(핸들러 :페이지
        (fn [db [_ 페이지]]
          (js/console.log "페이지전환 -> " 페이지)
          (assoc db :페이지 페이지)))

(핸들러 :미리보기
        (fn [db [_ on]] (assoc db :미리보기 (boolean on))))

(핸들러 :새프로젝트
        (fn [db [_ 프로젝트]]
          (when-let [아이디 (get-in db [:로그인 :이용자 :아이디])]
            (js/console.log #js ["새프로젝트 요청" 아이디 프로젝트])
            (POST (str "/" 아이디)
                {:내용 프로젝트
                 :성공 #(dispatch [:새프로젝트결과 :성공 %])
                 :실패 (fn [코드 내용] (dispatch [:새프로젝트결과 :실패 내용]))})
            (assoc db :새프로젝트 {:로딩? true :결과 {}}))))

(핸들러 :새프로젝트결과
        (fn [db [_ 성패 내용]]
          (js/console.log #js ["새프로젝트 결과" 성패 내용])
          (assoc db :새프로젝트 {:로딩? false})))

(def ^:private 채널 register-sub)

(채널 :알림
      (fn [db [_]]
        (reaction (:알림 @db))))

(채널 :가입신청
      (fn [db [_]]
        (reaction (or (:가입신청 @db) {}))))

(채널 :로그인
      (fn [db [_]]
        (reaction (or (:로그인 @db) {}))))

(채널 :페이지
      (fn [db [_]]
        (reaction (select-keys @db [:페이지 :미리보기]))))

(채널 :이용자
      (fn [db [_]]
        (reaction (get-in @db [:로그인 :이용자]))))

(채널 :새프로젝트
      (fn [db [_]]
        (reaction (or (:새프로젝트 @db) {}))))
