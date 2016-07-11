(ns ohucode.project
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ohucode.core :refer [다음버튼 링크 입력컨트롤
                                  패널 검증반응 유효-클래스 알림-div 페이지
                                  프로젝트-링크]]))

(defn- 저장소-주소 [이름공간 프로젝트명]
  (str "https://ohucode.com/" 이름공간 "/" 프로젝트명))

(defn- 빈저장소 [이름공간 프로젝트명]
  (let [주소 (저장소-주소 이름공간 프로젝트명)]
    [:div.container
     [:h4 "이미 로컬에 저장소가 있는 경우"]
     [:div [:pre "git remote add origin " 주소 "\n"
            "git push -u origin master"]]
     [:h4 "새로 저장소를 만드는 경우"]
     [:div [:pre
            "echo \"# " 프로젝트명 "\" >> README.md\n"
            "git init\n"
            "git add README.md\n"
            "git commit -m \"첫 커밋\"\n"
            "git remote add origin " 주소 "\n"
            "git push -u origin master"]]]))

(defn 프로젝트홈 []
  (let [프로젝트 (subscribe [:프로젝트])]
    (fn []
      (let [이름공간 (get-in @프로젝트 [:프로젝트 :소유자])
            프로젝트명 (get-in @프로젝트 [:프로젝트 :이름])]
        [페이지 [:h3 (프로젝트-링크 이름공간 프로젝트명)]
         [:div
          (if (:빈저장소? @프로젝트)
            (빈저장소 이름공간 프로젝트명)
            [:div "커밋이 있어요"])]]))))
