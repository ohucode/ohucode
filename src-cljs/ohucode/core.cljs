(ns ohucode.core
  (:require [reagent.core :as r]
            [cljsjs.marked]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [ajax.core :as ajax]
            [ajax.edn :refer [edn-request-format edn-response-format]]
            [ohucode.state :refer [앱상태 히스토리]]))

(def 서비스명 "오후코드")

(defn POST
  "AJAX POST 요청을 보냄. EDN 포맷으로 주고 받습니다.

  :내용 {}                    ; EDN 포맷으로 보낼 요청 본문
  :성공 (fn [응답내용])       ; 200류의 성공시 호출됨
  :실패 (fn [코드 응답내용])  ; 실패 또는 타임아웃시 호출됨
  :완료 (fn [])               ; 성패와 무관하게 마무리 작업에 사용"
  [url {:keys [내용 성공 실패 완료] :as 속성}]
  (ajax/POST url
      {:format (edn-request-format)
       :response-format (edn-response-format)
       :params 내용
       :timeout 3000
       :handler 성공
       :error-handler (fn [{:keys [status response]}] (실패 status response))
       :finally 완료}))

(defn 다음버튼 [속성]
  [:button.btn.btn-primary (dissoc 속성 :대기 :라벨)
   (or (:라벨 속성) "다음")
   " "
   (if (:대기 속성)
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
  [:div.alert.text-center {:class (str "alert-" (name 타입)) :role "alert"}
   텍스트])

(defn 패널
  "부트스트랩 panel. 제목은 .page-header로 감싸고, 내용은 뒷부분에 합친다."
  [제목 & 내용]
  (into [:div.panel.panel-ohucode>div.panel-body
         [:div.page-header (into [:h4] 제목)]]
        내용))

(defn prevent-default [핸들러]
  (fn [e]
    (.preventDefault e)
    (핸들러 e)))
