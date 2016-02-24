(ns ohucode.core
  (:require [reagent.core :as r]
            [cljsjs.jquery]
            [cljsjs.marked]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [ohucode.state :refer [앱상태 히스토리]]))

(def 서비스명 "오후코드")

(defn POST [url {data :data success :success :as 속성}]
  (js/console.log #js [url (pr-str data)])
  (js/$.ajax url  #js {:contentType "text/edn"
                       :method "POST"
                       :data (pr-str data) :success success}))

(defn 다음버튼 [속성]
  [:button.btn.btn-primary (dissoc 속성 :기다림 :텍스트)
   (or (:텍스트 속성) "다음")
   " "
   (if (:기다림 속성)
     [:i.fa.fa-spin.fa-spinner]
     [:i.fa.fa-angle-double-right])])

(defn 입력컨트롤 [속성 & 본문]
  (into [:input.form-control 속성] 본문))

(defn 문단 [제목 & 본문]
  (into [:div [:div.page-header>h2 제목]]
        본문))

(defn 마크다운 [속성]
  (let [src (r/atom "<i class='fa fa-spin fa-spinner'></i>")]
    (js/$.ajax #js {:url (:url 속성)
                    :cache false
                    :success #(reset! src (js/marked % #js {:sanitize true}))})
    (fn [속성] [:div {:dangerouslySetInnerHTML #js {:__html @src}}])))

(defn 사용자
  "로그인한 사용자정보"
  [] (get-in @앱상태 [:세션 :사용자]))

(defn 관리자?
  "로그인한 사용자에게 관리자 권한이 있나?"
  [] (= "admin" (:아이디 (사용자))))

(defn 링크
  "a 태그와 동일하지만, 페이지를 바꾸지 않고 라우팅 처리한다."
  [속성 & 본문]
  (let [href (:href 속성)]
    (into [:a (assoc 속성 :on-click (fn [e]
                                      (.preventDefault e)
                                      (.setToken 히스토리 href)))]
          본문)))

(defn 알림-div [타입 텍스트]
  [:div.alert {:class (str "alert-" (name 타입)) :role "alert"}
   [:button.close {:data-dismiss "alert" :aria-label "닫기"}
    [:i.fa.fa-close {:aria-hidden true}]] 텍스트])
