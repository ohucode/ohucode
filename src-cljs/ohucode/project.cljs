(ns ohucode.project
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ohucode.core :refer [다음버튼 링크 입력컨트롤
                                  패널 검증반응 유효-클래스 알림-div 페이지
                                  프로젝트-링크]]))

(defn 프로젝트홈 []
  (let [프로젝트 (subscribe [:프로젝트])]
    (fn []
      [페이지 [:h3 (프로젝트-링크
                    (get-in @프로젝트 [:프로젝트 :소유자])
                    (get-in @프로젝트 [:프로젝트 :이름]))]
       [:div "이제 무얼 할 수 있나요?"]])))
