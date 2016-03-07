(ns ohucode.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [cljsjs.marked]
            [cljsjs.highlight]
            [cljsjs.highlight.langs.clojure]
            [ajax.core :as ajax]
            [ajax.edn :refer [edn-request-format edn-response-format]]
            [ohucode.state :refer [히스토리]]
            [re-frame.core :refer [dispatch subscribe]]))

(def 서비스명 "오후코드")

(defn- wrap-edn-ajax [{:keys [내용 성공 실패 완료] :as 속성}]
  {:format (edn-request-format)
   :response-format (edn-response-format)
   :params 내용
   :timeout 3000
   :handler 성공
   :error-handler (fn [{:keys [status response]}] (실패 status response))
   :finally 완료})

(defn POST
  "AJAX POST 요청을 보냄. EDN 포맷으로 주고 받습니다.\n
  :내용 {}                    ; EDN 포맷으로 보낼 요청 본문
  :성공 (fn [응답내용])       ; 200류의 성공시 호출됨
  :실패 (fn [코드 응답내용])  ; 실패 또는 타임아웃시 호출됨
  :완료 (fn [])               ; 성패와 무관하게 마무리 작업에 사용"
  [url {:keys [내용 성공 실패 완료] :as 속성}]
  (ajax/POST url (wrap-edn-ajax 속성)))

(defn PUT
  "AJAX PUT 요청을 보냄. EDN 포맷으로 주고 받습니다.\n
  :내용 {}                    ; EDN 포맷으로 보낼 요청 본문
  :성공 (fn [응답내용])       ; 200류의 성공시 호출됨
  :실패 (fn [코드 응답내용])  ; 실패 또는 타임아웃시 호출됨
  :완료 (fn [])               ; 성패와 무관하게 마무리 작업에 사용"
  [url {:keys [내용 성공 실패 완료] :as 속성}]
  (ajax/PUT url (wrap-edn-ajax 속성)))

(defn DELETE
  "AJAX DELETE 요청을 보냄. EDN 포맷으로 주고 받습니다.\n
  :내용 {}                    ; EDN 포맷으로 보낼 요청 본문
  :성공 (fn [응답내용])       ; 200류의 성공시 호출됨
  :실패 (fn [코드 응답내용])  ; 실패 또는 타임아웃시 호출됨
  :완료 (fn [])               ; 성패와 무관하게 마무리 작업에 사용"
  [url {:keys [내용 성공 실패 완료] :as 속성}]
  (ajax/DELETE url (wrap-edn-ajax 속성)))

(defn prevent-default
  "기본 이벤트 처리를 무시하고 별도 처리하는 핸들러.
  단순히 .preventDefault를 호출하고 진행함."
  [핸들러]
  (fn [e]
    (.preventDefault e)
    (핸들러 e)
    false))

(defn 다음버튼 [{:keys [클릭 라벨 로딩?] :as 속성}]
  (let [온클릭 (if (ifn? 클릭) {:on-click (prevent-default 클릭)})]
    [:button.btn.btn-primary
     (merge 온클릭 (dissoc 속성 :로딩? :라벨 :클릭))
     (or 라벨 "다음")
     " "
     (if 로딩? [:i.fa.fa-spin.fa-spinner])]))

(defn 입력컨트롤 [속성 & 본문]
  (into [:input.form-control 속성] 본문))

(defn 문단 [제목 & 본문]
  (into [:div [:div.page-header>h2 제목]]
        본문))

(defn 마크다운 [속성]
  (let [src (r/atom "<i class='fa fa-spin fa-spinner'></i>")]
    (ajax/GET (:url 속성)
        {:timeout 3000
         :handler #(reset! src (js/marked % #js {:sanitize true}))
         :error-handler #(reset! src "실패: " %1)})
    (fn [속성] [:div {:dangerouslySetInnerHTML #js {:__html @src}}])))

(defn 관리자?
  "로그인한 사용자에게 관리자 권한이 있나?"
  [아이디] (= "admin" 아이디))

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

(defn 이벤트
  "링크를 클릭하면 이벤트 발생(dispatch)하는 함수.
  이벤트는 dispatch 함수와 같이 벡터 형태로 전달한다."
  [이벤트 & 본문]
  (into [:a {:href "#" :on-click (prevent-default #(dispatch 이벤트))}] 본문))

(defn 화면이동
  "화면에 보여줄 메인 페이지를 전환하는 a 태그."
  [속성 & 본문]
  (let [페이지 (:페이지 속성)]
    (이벤트 [:페이지 페이지] 본문)))

(defn 검증함수
  "오후코드 애플리케이션 전체에서 입력값 검증을 위해 사용하는 공통함수.
  옳은 형태의 아이디인지 확인하려면 (검증함수 :아이디)를 써서 확인하자.\n
  ((검증함수 :아이디) \"hatemogi\") ; => true"
  [키] {:post [(fn? %)]}
  (let [re검증 #(comp boolean (partial re-matches %))]
    ({:아이디   (re검증 #"[가-힣\w][가-힣\w_\-]{3,15}")
      :비밀번호 #(<= 7 (count %))
      :이메일   (re검증 #".+@.+\..+")
      :성명     (re검증 #"[가-힝\w]{2,5}")} 키)))

(defn 유효?
  "검증함수를 기준으로 값을 확인한다.
  값이 nil일 경우 별도로 처리해서 nil이고,
  그렇지 않은 경우 검증함수에 따라 true/false가 된다."
  [키 값]
  (cond
    (nil? 값) nil
    (ifn? 값) (유효? 키 (값 키))
    :else     ((검증함수 키) 값)))

(defn 검증반응
  "폼상태 atom으로 부터 유효성을 검증한 결과 반응(reagent.ratom/reaction)
  @폼상태의 요소중 키목록에 있는 값들에 대해 [유효?]함수를 써서 검증.
  모든 값이 참이면 {:유효 true, :무효 false}도 merge됨.\n
  키목록에 있는 키워드는 검증함수에서 처리가능하도록 미리 등록돼 있어야한다.\n
  (검증반응 폼상태 [:아이디 :비밀번호]) ; => (reagent.core/atom)"
  [폼상태 키목록]
  (reaction
   (let [결과 (zipmap 키목록 (map #(유효? % @폼상태) 키목록))
         유효 (every? boolean (vals 결과))]
     (merge {:유효 유효 :무효 (not 유효)} 결과))))

(defn 유효-클래스
  "부트스트랩용 폼 유효성 css 클래스를 표현하는 맵.\n
  true -> {:class \"has-success\"}
  false -> {:class \"has-error\"}
  nil -> {:class \"\"}"
  [검증상태]
  {:class (case 검증상태
            true "has-success"
            false "has-error"
            "")})
