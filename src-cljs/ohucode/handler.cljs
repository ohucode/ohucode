(ns ohucode.handler
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST PUT GET 검증반응]]))

(def ^{:doc "애플리케이션 상태 초기화"}
  초기상태 {:가입신청 {}                  ; 가입신청시 신청 정보 임시 보관
            :가입결과 {}                  ; 가입신청 결과 성패와 설명
            :이용자 {}                    ; 로그인한 이용자 정보
            :페이지 :첫화면               ; <main></main> 화면에 보여줄 페이지
            :첫화면 {:가입or로그인 :가입}
            :미리보기 false               ; 개발용 미리보기 모드 on/off
            :새프로젝트 {}                ; 새프로젝트 신청/결과
            :공간 {}                      ; 선택한 공간(네임스페이스)
            :프로젝트 {}                  ; 선택한 프로젝트
            })

(register-handler
 :초기화
 (fn [_ _] 초기상태))

(register-handler
 :첫화면선택
 (fn [db [_ 선택 값]]
   (js/console.log "첫화면선택" 선택 값)
   (assoc-in db [:첫화면 선택] 값)))

(register-handler
 :가입신청
 (fn [db [_ 이용자]]
   (js/console.log "가입신청" (str 이용자))
   (POST "/signup"
       {:내용 이용자
        :성공 (fn [내용] (dispatch [:가입신청-결과 :성공 내용]))
        :실패 (fn [코드 내용] (dispatch [:가입신청-결과 :실패 내용]))})
   (assoc db :가입신청 {:로딩? true
                        :이용자 이용자
                        :결과 {}})))

(register-handler
 :가입신청-결과
 (fn [db [_ 성패 내용]]
   (js/console.log "가입결과" (name 성패))
   (cond-> (assoc-in db [:가입신청 :로딩?] false)
     (= :실패 성패) (assoc-in [:가입신청 :실패] "실패했어요"))))

(register-handler
 :로그인요청
 (fn [db [_ 이용자]]
   (js/console.log #js ["로그인 요청" 이용자])
   (POST "/user/login"
       {:내용 이용자
        :성공 #(dispatch [:로그인요청-결과 :성공 %])
        :실패 (fn [코드 내용] (dispatch [:로그인요청-결과 :실패 내용]))})
   (assoc db :로그인 {:로딩? true :결과 {}})))

(register-handler
 :로그인요청-결과
 (fn [db [_ 성패 내용]]
   (js/console.log "로그인결과" (name 성패) (str 내용))
   (dispatch [:페이지 :이용자홈])
   (if (= :성공 성패) (dispatch [:로그인 (:이용자 내용)]))
   (assoc-in db [:로그인 :로딩?] false)))

(register-handler
 :로그인
 (fn [db [_ 이용자]]
   (assoc-in db [:로그인 :이용자] 이용자)))

(register-handler
 :로그아웃
 (fn [db _]
   (dispatch [:페이지 :로그아웃])
   (PUT "/user/logout"
       {:내용 {}
        :성공 #(dispatch [:로그아웃-결과 :성공])
        :실패 #(dispatch [:로그아웃-결과 :실패])})
   db))

(register-handler
 :로그아웃-결과
 (fn [db [_ 성패]]
   (js/console.log "로그아웃결과" (name 성패))
   (if (= :성공 성패)
     (dissoc db :로그인)
     (assoc db :알림 "실패"))))

(register-handler
 :페이지
 (fn [db [_ 페이지]]
   (js/console.log "페이지전환 -> " 페이지)
   (assoc db :페이지 페이지)))

(register-handler
 :미리보기
 (fn [db [_ on]]
   (assoc db :미리보기 (boolean on))))

(register-handler
 :새프로젝트
 (fn [db [_ 프로젝트]]
   (when-let [아이디 (get-in db [:로그인 :이용자 :아이디])]
     (js/console.log #js ["새프로젝트 요청" 아이디 프로젝트])
     (POST (str "/" 아이디)
         {:내용 프로젝트
          :성공 #(dispatch [:새프로젝트-결과 :성공 %])
          :실패 (fn [코드 내용] (dispatch [:새프로젝트-결과 :실패 내용]))})
     (assoc db :새프로젝트 {:로딩? true :결과 {}}))))

(register-handler
 :새프로젝트-결과
 (fn [db [_ 성패 내용]]
   (js/console.log #js ["새프로젝트 결과" 성패 내용])
   (assoc db :새프로젝트 {:로딩? false})))

(register-handler
 :공간선택
 (fn [db [_ ns]]
   (js/console.log "공간선택 register-handler" ns)
   (GET (str "/" ns)
       {:성공 #(dispatch [:공간선택-결과 :성공 %])
        :실패 (fn [코드 내용] (dispatch [:공간선택-결과 :실패 내용]))})
   (assoc db :공간 {:로딩? true})))

(register-handler
 :공간선택-결과
 (fn [db [_ 성패 내용]]
   (js/console.log (clj->js 내용))
   (assoc db :공간 (merge 내용 {:로딩? false}))))

(register-handler
 :프로젝트선택
 (fn [db [_ ns project]]
   (js/console.log "프로젝트선택 register-handler" ns)
   (GET (str "/" ns "/" project)
       {:성공 #(dispatch [:프로젝트선택-결과 :성공 %])
        :실패 (fn [코드 내용] (dispatch [:프로젝트선택-결과 :실패 내용]))})
   (dispatch [:페이지 :프로젝트홈])
   (assoc db :프로젝트 {:로딩? true})))

(register-handler
 :프로젝트선택-결과
 (fn [db [_ 성패 내용]]
   (js/console.log (clj->js 내용))
   (assoc db :프로젝트 (merge 내용 {:로딩? false}))))

;;; 이하 register-sub은 subscribe를 위한 쿼리 함수를 등록한다.
;;; 그리고 이 함수는 뷰 컴포넌트에서 subscribe로 불러다 쓴다.

(register-sub
 :알림
 (fn [db [_]]
   (reaction (:알림 @db))))

(register-sub
 :첫화면선택
 (fn [db [_ 선택]]
   (reaction (get-in @db [:첫화면 선택]))))

(register-sub
 :가입신청
 (fn [db [_]]
   (reaction (or (:가입신청 @db) {}))))

(register-sub
 :로그인
 (fn [db [_]]
   (reaction (or (:로그인 @db) {}))))

(register-sub
 :페이지
 (fn [db [_]]
   (reaction (select-keys @db [:페이지 :미리보기]))))

(register-sub
 :이용자
 (fn [db [_]]
   (reaction (get-in @db [:로그인 :이용자]))))

(register-sub
 :새프로젝트
 (fn [db [_]]
   (reaction (or (:새프로젝트 @db) {}))))

(register-sub
 :공간
 (fn [db [_]]
   (reaction (or (:공간 @db) {}))))
