(ns ohucode.user
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ohucode.core :refer [다음버튼 링크 입력컨트롤
                                  패널 검증반응 유효-클래스 알림-div 페이지]]))

(defn 가입폼 [& 선택]
  (let [fg :div.form-group
        키목록 [:이메일 :아이디 :비밀번호 :성명]
        폼상태 (r/atom {})
        검증상태 (검증반응 폼상태 키목록)
        신청상태 (subscribe [:가입신청])
        입력 (fn [키 속성]
               [fg (유효-클래스 (@검증상태 키))
                [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (@폼상태 키)
                                    :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                   속성)]])]
    (fn [속성]
      [패널 [[:i.fa.fa-user-plus] " 가입 신청"]
       (if-let [실패 (@신청상태 :실패)]
         [알림-div :warning 실패])
       [:form
        [:fieldset {:disabled (@신청상태 :로딩?)}
         [입력 :이메일   {:type "email" :auto-focus true :auto-complete "email"}]
         [입력 :아이디   {:auto-complete "username"}]
         [입력 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [입력 :성명     {:auto-complete "name"}]
         [fg [다음버튼 {:라벨 "가입" :로딩? (@신청상태 :로딩?)
                        :disabled (@검증상태 :무효)
                        :class "btn-block btn-lg"
                        :클릭 #(dispatch [:가입신청 (select-keys @폼상태 키목록)])}]]
         [fg [:div.text-center "가입하면 오후코드의 "
              [링크 {:href "/tos"} "약관"] " 및 "
              [링크 {:href "/policy"} "개인정보 취급방침"] "에 동의하시게 됩니다."]]]]])))

(defn 가입환영 []
  [페이지 "환영합니다"
   [:div "이제 무얼 할 수 있나요?"]])

(defn 로그인폼 []
  (let [fg :div.form-group
        폼상태 (r/atom {})
        키목록 [:아이디 :비밀번호]
        검증상태 (검증반응 폼상태 키목록)
        입력 (fn [키 속성]
               [fg (유효-클래스 (@검증상태 키))
                [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (@폼상태 키)
                                    :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                   속성)]])
        로그인상태 (subscribe [:로그인])]
    (fn []
      [패널 [[:i.fa.fa-sign-in] " 로그인"]
       [:form
        [:fieldset {:disabled (@로그인상태 :로딩?)}
         [입력 :아이디 {:auto-focus true :auto-complete "username"
                        :placeholder "아이디 또는 이메일"}]
         [입력 :비밀번호 {:type "password" :auto-complete "current-password"}]
         [fg [다음버튼 {:라벨 "로그인" :로딩? (@로그인상태 :로딩?)
                        :disabled (@검증상태 :무효)
                        :class "btn-block"
                        :클릭 #(dispatch [:로그인요청 (select-keys @폼상태 키목록)])}]]]]])))

(defn 프로젝트목록 [플젝들]
  [페이지 [:h3 "프로젝트"]
   [:ul.list-group
    (for [플젝 플젝들]
      (let [키 (str "/" (:소유자 플젝) "/" (:이름 플젝))]
        [:li.list-group-item {:key 키}
         [링크 {:페이지 키} [:h4 (:이름 플젝)]]]))]])

(defn 공간첫페이지 []
  (let [공간 (subscribe [:공간])]
    (fn []
      [:div.container-fluid
       [:div.row
        [:div.col-md-3
         [:div.thumbnail
          [:img {:src "https://pbs.twimg.com/profile_images/649229866798157824/km1HyMU-_400x400.jpg" :alt "프로필 이미지"}]
          [:div.caption
           [:h3 (get-in @공간 [:공간주인 :성명] "이름없음")
            [:small (get-in @공간 [:공간주인 :아이디])]]]]]
        [:div.col-md-9 [프로젝트목록 (:플젝목록 @공간)]]]])))

(defn 새프로젝트 [아이디]
  (let [fg :div.form-group
        폼상태 (r/atom {:공개? true :초기화? false})
        검증상태 (검증반응 폼상태 [:프로젝트명])
        새프로젝트상태 (subscribe [:새프로젝트])
        입력 (fn [키 속성]
               [입력컨트롤 (merge {:type "text" :placeholder (name 키) :value (@폼상태 키)
                                   :on-change #(swap! 폼상태 assoc 키 (.-target.value %))}
                                  속성)])
        라디오 (fn [키 속성]
                 [:input (merge {:type "radio" :checked (= (속성 :값) (@폼상태 키))
                                 :on-change #(swap! 폼상태 assoc 키 (속성 :값))}
                                속성)])
        체크박스 (fn [키]
                   [:input {:type "checkbox" :checked (@폼상태 키)
                            :on-change #(swap! 폼상태 update 키 not)}])]
    (fn [아이디]
      [패널 ["새 프로젝트"]
       [:div.새프로젝트
        [:form.form-inline
         [:fieldset {:disabled (@새프로젝트상태 :로딩?)}
          [fg
           [:select.form-control [:option {:value "hatemogi"} "hatemogi"]]
           " / "
           [입력 :프로젝트명 {:auto-focus true}]]]]
        [:br]
        [:form
         [:fieldset {:disabled (@새프로젝트상태 :로딩?)}
          [fg
           [입력 :설명 {:placeholder "설명"}]]
          [:hr]
          [:div.radio [:label [라디오 :공개? {:값 true}]
                       [:span {:class "octicon mega-octicon octicon-repo 공개아이콘"}]
                       [:dl
                        [:dt "공개 저장소"]
                        [:dd "누구나 프로젝트 내용을 볼 수 있고, 커밋할 수 있는 사람들을 따로 지정할 수 있습니다."]]]]
          [:div.radio [:label [라디오 :공개? {:값 false}]
                       [:span {:class "octicon mega-octicon octicon-lock 공개아이콘"}]
                       [:dl
                        [:dt "비공개 저장소"]
                        [:dd "누가 이 프로젝트를 보고 쓸 수 있는지 따로 지정합니다."]]]]
          [:hr]
          [:div.checkbox [:label [체크박스 :초기화?] "README 파일과 함께 프로젝트 초기화"]]
          [fg [:div (str @폼상태)]]
          [fg [다음버튼 {:라벨 "만들기" :로딩? (@새프로젝트상태 :로딩?)
                         :disabled (@검증상태 :무효)
                         :클릭 #(dispatch [:새프로젝트
                                           (select-keys @폼상태
                                                        [:프로젝트명 :설명 :공개? :초기화?])])}]]]]]])))

(defn 로그아웃 []
  (let [이용자 (subscribe [:이용자])]
    (fn []
      [페이지
       [:h2 "로그아웃"]
       (if @이용자
         [:div "로그아웃 중입니다."]
         [:section
          [:div "수고하셨습니다"]
          [링크 {:href "/"} "첫페이지로 이동"]])])))
