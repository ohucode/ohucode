(ns ohucode.top
  (:require [reagent.core :as r]
            [ohucode.state :refer [앱상태]]
            [ohucode.signup :as 가입]
            [ohucode.core :refer [서비스명 문단 마크다운 링크 사용자 관리자? prevent-default]]
            [cljsjs.bootstrap :as b]))

(defn 이용약관 []
  [:div.container-fluid
   [문단 "서비스이용약관"
    [:div "오후코드 서비스를 이용하시면, 아래와..."]]])

(defn 개인정보취급방침 []
  [:div.container-fluid
   [문단 "개인정보취급방침"
    [:div "개인정보를 중요하게 생각합니다."]]])

(defn 감사의말 []
  (문단 "고마움을 전합니다"
        [마크다운 {:url "/md/CREDITS.md"}]))

(defn 첫페이지 []
  [:div
   [:div.jumbotron
    [:div.row
     [:div.col-xs-6.col-md-8
      [:h1 서비스명]
      [:p "즐겁고 효율적인 프로그래밍의 동반자, " 서비스명 "에 오신 것을 환영합니다. "
       서비스명 "는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git 리모트 리포지토리)를 "
       "편리하게 제공합니다."]]
     [:div.col-xs-6.col-md-4
      [가입/신청폼]
      [:div.panel.panel-login
       [:div.panel-body.text-center
        [:div "계정이 있으신가요? "
         [링크 {:href "/login"} "로그인"]]]]]]]
   [:div.container>div.row
    [:div.page-header [:h1 "Git 저장소 서비스"]]
    [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
    [:div.page-header [:h1 "위키 페이지 작성"]]]])

(def 계정정보메뉴
  (with-meta
    (fn [속성]
      [:li.dropdown
       [:a.dropdown-toggle
        {:id "accountMenu1" :role "button" :data-toggle "dropdown"
         :aria-haspopup true :aria-expanded true}
        "hatemogi" " " [:span.caret]]
       [:ul.dropdown-menu {:aria-labelledby "accountMenu1"}
        [:li [링크 {:href "/user/profile"}   [:i.fa.fa-fw.fa-user]     " 프로필"]]
        [:li [링크 {:href "/user/message"}   [:i.fa.fa-fw.fa-envelope] " 메시지"]]
        [:li [링크 {:href "/user/bookmarks"} [:i.fa.fa-fw.fa-bookmark] " 책갈피"]]
        [:li.divider {:role "separator"}]
        [:li [링크 {:href "/user/settings"}  [:i.fa.fa-fw.fa-cog]      " 설정"]]
        [:li [링크 {:href "/user/logout"}    [:i.fa.fa-fw.fa-sign-out] " 로그아웃"]]]])
    {:component-did-mount #(js/console.log "계정정보 마운트됨")}))

(defn 네비게이션 [속성]
  (let [toggle-preview-mode
        (fn [e]
          (swap! 앱상태 assoc :미리보기 (not (:미리보기 @앱상태))))]
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
        [:li {:class (if (:미리보기 @앱상태) "active" "")}
         [:a {:href "#" :on-click (prevent-default toggle-preview-mode)} "미리보기"]]
        [:li [링크 {:href "/help"} "도움말"]]]
       (if-let [사용자 {:아이디 "hatemogi"}]
         [:ul.nav.navbar-nav.navbar-right
          (if (관리자?)
            [:li [:a {:href "/admin"} "관리자"]])
          [:li
           [링크 {:href "/new" :title "새 저장소 만들기"} [:span.octicon.octicon-plus]]]
          [계정정보메뉴]]
         [:ul.nav.navbar-nav.navbar-right
          [:li [:a {:href "/user/login"} [:i.fa.fa-sign-in] " 로그인"]]])]]]))

(defn 꼬리말 []
  [:div.container
   [:div.row
    [:ul.list-inline
     [:li "Copyright " [:i.fa.fa-copyright] " 2016 " 서비스명]
     [:li [링크 {:href "/policy"} "개인정보보호정책"]]
     [:li [링크 {:href "/tos"} "이용약관"]]
     [:li [링크 {:href "/credits"} "감사의 말"]]]]])

(defn 빈페이지 []
  [:div (str (:페이지 @앱상태))])

(defn- 보기 [페이지 제목]
  [:a {:href "#"} 제목])

(defn main-view []
  [(case (:페이지 @앱상태)
     :첫페이지 첫페이지
     :이용약관 이용약관
     :감사의말 감사의말
     :개인정보취급방침 개인정보취급방침
     빈페이지)])

(defn 미리보기
  "미리보기 페이지"
  []
  (let [미리보기목록 [["가입신청" 가입/신청폼]]
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
     [:div.col-md-10 [main-view]]]))

(defn 앱페이지 []
  [:div
   [:nav [네비게이션]]
   [:main
    [:div.container-fluid (if (:미리보기 @앱상태)
                            [미리보기]
                            [main-view])]]
   [:footer [꼬리말]]])
