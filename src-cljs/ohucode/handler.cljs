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
            })

(register-handler :가입폼
                  (fn [db [_ 키 값]]
                    #_(js/console.log "가입폼" 값)
                    (assoc-in db [:가입폼 키] 값)))

(register-handler :가입신청
                  (fn [db [_ 이용자]]
                    (js/console.log "가입신청" (str 이용자))
                    (POST "/signup"
                        {:내용 이용자
                         :성공 (fn [내용] (dispatch [:가입결과 :성공 내용]))
                         :실패 (fn [코드 내용] (dispatch [:가입결과 :실패 내용]))})
                    (assoc db :가입신청 {:로딩? true
                                         :이용자 이용자
                                         :결과 {}})))

(register-handler :가입결과
                  (fn [db [_ 성패 내용]]
                    (js/console.log "가입결과" (name 성패))
                    (cond-> (assoc-in db [:가입신청 :로딩?] false)
                      (= :실패 성패) (assoc-in [:가입신청 :실패] "실패했어요"))))

(register-handler :로그인
                  (fn [db [_ 이용자]]
                    (js/console.log #js ["로그인 요청" 이용자])
                    (POST "/user/login"
                        {:내용 이용자
                         :성공 #(dispatch [:로그인결과 :성공 %])
                         :실패 (fn [코드 내용] (dispatch [:로그인결과 :실패 내용]))})
                    (assoc db :로그인 {:로딩? true
                                       :이용자 (select-keys 이용자 [:아이디])
                                       :결과 {}})))

(register-handler :로그인결과
                  (fn [db [_ 성패 내용]]
                    (js/console.log "로그인결과" (name 성패) (str 내용))
                    (dispatch [:페이지 :이용자홈])
                    (cond-> (assoc-in db [:로그인 :로딩?] false)
                      (= :성공 성패) (assoc :이용자 (:이용자 내용)))))

(register-handler :로그아웃
                  (fn [db _]
                    (PUT "/user/logout"
                        {:내용 {}
                         :성공 #(dispatch [:로그아웃결과 :성공])
                         :실패 #(dispatch [:로그아웃결과 :실패])})
                    db))

(register-handler :로그아웃결과
                  (fn [db [_ 성패]]
                    (js/console.log "로그아웃결과" (name 성패))
                    (dispatch [:페이지 :이용자홈])
                    (if (= :성공 성패)
                      (do
                        (dispatch [:페이지 :첫페이지>가입])
                        (dissoc db :이용자))
                      (assoc db :알림 "실패"))))

(register-handler :페이지
                  (fn [db [_ 페이지]]
                    (js/console.log "페이지전환 -> " 페이지)
                    (assoc db :페이지 페이지)))

(register-handler :미리보기
                  (fn [db [_ on]]
                    (js/console.log "미리보기모드 -> " on)
                    (assoc db :미리보기 (boolean on))))

(register-sub :알림
              (fn [db [_]]
                (reaction (:알림 @db))))

(register-sub :가입신청
              (fn [db [_]]
                (reaction (or (:가입신청 @db) {}))))

(register-sub :로그인
              (fn [db [_]]
                (reaction (or (:로그인 @db) {}))))

(register-sub :페이지
              (fn [db [_]]
                (reaction (select-keys @db [:페이지 :미리보기]))))

(register-sub :이용자
              (fn [db [_]]
                (reaction (:이용자 @db))))
