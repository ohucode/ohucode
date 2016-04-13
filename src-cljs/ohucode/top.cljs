(ns ohucode.top
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [ohucode.user :as 이용자]
            [ohucode.core :refer [서비스명 페이지 마크다운 관리자? 링크]]
            [cljsjs.bootstrap :as b]))

(defn 이용약관 []
  [:div.container-fluid
   [페이지 [:h2 "서비스이용약관"]
    [:div "오후코드 서비스를 이용하시면, 아래와..."]]])

(defn 개인정보취급방침 []
  [:div.container-fluid
   [페이지 [:h2 "개인정보취급방침"]
    [:div "개인정보를 중요하게 생각합니다."]]])

(defn 감사의말 []
  (페이지 [:h2 "고마움을 전합니다"]
          [마크다운 {:url "/md/CREDITS.md"}]))

(defn 첫페이지 [가입or로그인]
  [:div
   [:div.jumbotron
    [:div.row
     [:div.col-xs-6.col-md-8
      [:h1 서비스명]
      [:p "즐겁고 효율적인 프로그래밍의 동반자, " 서비스명 "에 오신 것을 환영합니다. "
       서비스명 "는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git 리모트 리포지토리)를 "
       "편리하게 제공합니다."]]
     (case 가입or로그인
       :가입 [:div.col-xs-6.col-md-4
              [이용자/가입폼]
              [:div.panel.panel-login
               [:div.panel-body.text-center
                [:div "이미 가입하셨나요? "
                 [링크 {:페이지 :첫페이지>로그인} "로그인"]]]]]
       :로그인 [:div.col-xs-6.col-md-4
                [이용자/로그인폼]
                [:div.panel.panel-login
                 [:div.panel-body.text-center
                  [:div "아이디가 없으신가요? "
                   [링크 {:페이지 :첫페이지>가입} "가입하기"]]]]]
       [:div "페이지 상태 에러"])]]
   [:div.container>div.row
    [:div.page-header [:h1 "Git 저장소 서비스"]]
    [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
    [:div.page-header [:h1 "위키 페이지 작성"]]]])

(defn 계정정보메뉴 [아이디]
  [:li.dropdown
   [:a.dropdown-toggle
    {:id "accountMenu1" :role "button" :data-toggle "dropdown"
     :aria-haspopup true :aria-expanded true}
    아이디 " " [:span.caret]]
   [:ul.dropdown-menu {:aria-labelledby "accountMenu1"}
    [:li [링크 {:href (str "/" 아이디)}              [:i.fa.fa-fw.fa-user]     " 프로필"]]
    [:li [링크 {:href (str "/" 아이디 "/messages")}  [:i.fa.fa-fw.fa-envelope] " 메시지"]]
    [:li [링크 {:href (str "/" 아이디 "/bookmarks")} [:i.fa.fa-fw.fa-bookmark] " 책갈피"]]
    [:li.divider {:role "separator"}]
    [:li [링크 {:href (str "/" 아이디 "/settings")}  [:i.fa.fa-fw.fa-cog]      " 설정"]]
    [:li [링크 {:이벤트 [:로그아웃]}                 [:i.fa.fa-fw.fa-sign-out] " 로그아웃"]]]])

(defn 네비게이션 [미리보기]
  (let [이용자 (subscribe [:이용자])]
    (fn [미리보기]
      [:nav.navbar.navbar-inverse.navbar-static-top
       [:div.container-fluid
        [:div.navbar-header
         [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse"
                                           :data-target "#navbar" :aria-expanded false
                                           :aria-controls "navbar"}
          [:span.sr-only "내비게이션 여닫기"]
          [:span.icon-bar][:span.icon-bar][:span.icon-bar]]
         [링크 {:class "navbar-brand" :href "/"} [:i.fa.fa-git-square] " " 서비스명]]
        [:div#navbar.collapse.navbar-collapse
         [:ul.nav.navbar-nav
          ;; TODO: 개발자만(또는 개발환경에서만) 미리보기모드를 제공합니다.
          [:li {:class (if 미리보기 "active" "")}
           [링크 {:이벤트 [:미리보기 (not 미리보기)]} "미리보기"]]
          [:li [링크 {:href "/help"} "도움말"]]]
         (if-let [아이디 (:아이디 @이용자)]
           [:ul.nav.navbar-nav.navbar-right
            (if (관리자? 아이디)
              [:li [링크 {:href "/admin"} "관리자"]])
            [:li
             [링크 {:페이지 :새프로젝트 :title "새 프로젝트 만들기"} [:span.octicon.octicon-plus]]]
            [계정정보메뉴 아이디]]
           [:ul.nav.navbar-nav.navbar-right
            [:li [링크 {:페이지 :첫페이지>로그인} [:i.fa.fa-sign-in] " 로그인"]]])]]])))

(defn 꼬리말 []
  [:div.container
   [:div.row
    [:ul.list-inline
     [:li "Copyright " [:i.fa.fa-copyright] " 2016 " 서비스명]
     [:li [링크 {:href "/policy"} "개인정보보호정책"]]
     [:li [링크 {:href "/tos"} "이용약관"]]
     [:li [링크 {:href "/credits"} "감사의 말"]]]]])

(defn 빈페이지 []
  [:div "빈페이지"])

(defn- 보기 [페이지 제목]
  [:a {:href "#"} 제목])

(defn main-view []
  (let [페이지 (subscribe [:페이지])]
    (fn []
      (loop [페이지 (:페이지 @페이지)]
        (cond
          (keyword? 페이지) (case 페이지
                              :첫페이지>가입 [첫페이지 :가입]
                              :첫페이지>로그인 [첫페이지 :로그인]
                              :이용약관 [이용약관]
                              :감사의말 [감사의말]
                              :개인정보취급방침 [개인정보취급방침]
                              :가입신청 [이용자/가입폼]
                              :가입환영 [이용자/가입환영]
                              :공간첫페이지 [이용자/공간첫페이지
                                             {:아이디 "애월조단" :성명 "김대현"
                                              :프로젝트 [{:소유자 "애월조단" :이름 "오후코드"}
                                                         {:소유자 "애월조단" :이름 "빈프로젝트"}]}]
                              :새프로젝트 [이용자/새프로젝트]
                              [빈페이지])
          (var? 페이지) [(deref 페이지)]
          (fn? 페이지) [페이지]
          (vector? 페이지) (recur (first 페이지))
          :기타 [빈페이지])))))

(defn 미리보기
  "미리보기 페이지"
  [본문]
  (let [목록 [:첫페이지>가입
              :첫페이지>로그인
              :이용약관
              :감사의말
              :개인정보취급방침
              :가입신청
              :가입환영
              #'ohucode.user/로그인폼
              :공간첫페이지
              :새프로젝트]
        이름 (fn [대상]
               (cond
                 (keyword? 대상) (name 대상)
                 (var? 대상) (-> 대상 meta :name str)
                 :기타 "음?!"))
        보기 (fn [페이지]
               [링크 {:페이지 페이지} (이름 페이지)])]
    [:div.row
     [:div.col-md-2
      [:ul.list-group
       (for [페이지 목록]
         ^{:key (이름 페이지)} [:li.list-group-item [보기 페이지]])]]
     [:div.col-md-10 [본문]]]))

(defn 앱페이지 []
  (let [페이지 (subscribe [:페이지])]
    (fn []
      [:div
       [:nav [네비게이션 (:미리보기 @페이지)]]
       [:main
        [:div.container-fluid
         (if (:미리보기 @페이지)
           [미리보기 main-view]
           [main-view])]]
       [:footer [꼬리말]]])))
