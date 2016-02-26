(ns ^{:doc "오후코드 뷰 미리보기"}
    ^{:author "김대현"}
    ohucode.preview
  (:require [ohucode.state :refer [앱상태]]
            [reagent.core :as r]
            [ohucode.signup :as 가입]))

(defn- 보기 [페이지 제목]
  [:a {:href "#"} 제목])

(defn 미리보기
  "미리보기 페이지"
  []
  (let [미리보기목록 [["가입신청-1" 가입/신청1]
                      ["가입신청-2" 가입/신청2]
                      ["가입신청-3" 가입/신청3]]
        보기 (fn [페이지 텍스트]
               [:a {:href "#"
                    :on-click (fn [e]
                                (.preventDefault e)
                                (swap! 앱상태 assoc :페이지 페이지))}
                텍스트])]
    [:div.row
     [:div.col-md-2
      [:ul.list-group
       (for [[텍스트 페이지] 미리보기목록]
         ^{:key 텍스트} [:li.list-group-item [보기 페이지 텍스트]])]]
     [:div.col-md-10 [@앱상태 :페이지]]]))
