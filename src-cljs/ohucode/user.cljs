(ns ohucode.user
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]))

(defn 이용자홈 []
  [:div "로그인한 이용자 홈 화면"])
