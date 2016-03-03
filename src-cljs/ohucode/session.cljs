(ns ohucode.session
  (:require [reagent.core :as r]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [ohucode.core :refer [POST 서비스명 다음버튼 링크 입력컨트롤 알림-div prevent-default 패널]]
            [ohucode.state :refer [앱상태]]))

(defn- 폼그룹 [속성 & 입력부]
  (into [:div.form-group (dissoc 속성 :라벨)] 입력부))

(defn 로그인폼 [속성]
  (let []
    (fn [속성]
      [:div "로그인폼"

       [패널 "로그인"
        [:form
         [:fieldset

          ]]]])))
