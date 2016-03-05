(ns ohucode.handler
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST 검증반응]]))

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
                    (assoc db :가입신청 {:요청중? true
                                         :이용자 이용자
                                         :결과 {}})))

(register-handler :가입결과
                  (fn [db [_ 성패 내용]]
                    (js/console.log "가입결과" (name 성패))
                    (cond-> (assoc-in db [:가입신청 :요청중?] false)
                      (= :실패 성패) (assoc-in [:가입신청 :실패] "실패했어요"))))

(register-handler :로그인
                  (fn [db [_ 이용자]]
                    (js/console.log ["로그인 했군요." 이용자])
                    (assoc db :이용자 이용자)))

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

(register-sub :페이지
              (fn [db [_]]
                (reaction (select-keys @db [:페이지 :미리보기]))))

(register-sub :이용자
              (fn [db [_]]
                (reaction {:이용자 @db})))
