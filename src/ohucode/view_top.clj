(ns ohucode.view-top
  (:use [hiccup.core]
        [hiccup.page]
        [ohucode.core]
        [ohucode.view]
        [ohucode.view-signup]))

(defn basic-content [req title & body]
  (layout req {:title (brand-name+ "> " title)}
          [:div.container [:div.row [:h1 title] body]]))

(defn not-found [req]
  {:status 404
   :body (basic-content req "찾을 수 없습니다"
                        [:p "요청하신 페이지를 찾을 수 없습니다."])})

(defn request-error [req message]
  {:status 403
   :body (basic-content req "입력 값 오류"
                        [:p message])})

(defn terms-of-service [req]
  (basic-content req "서비스 이용약관"
                 [:p "오후코드 서비스를 이용하면..."]))

(defn privacy-policy [req]
  (basic-content req "개인정보 보호정책"
                 [:p "오후코드 서비스를 이용하면..."]))

(defn intro-guest [req]
  (layout req {:title (brand-name+ "첫화면")}
          [:div.jumbotron
           [:div.row
            [:div.col-xs-6.col-md-8
             [:h1 brand-name]
             [:p "즐겁고 효율적인 프로그래밍의 동반자, " brand-name "에 오신 것을 환영합니다. "
              brand-name "는 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git 리모트 리포지토리)를 편리하게 제공합니다."]]
            [:div.col-xs-6.col-md-4
             [:div.panel.panel-signup
              [:div.panel-body
               [:div.page-header [:h4 [:i.fa.fa-user-plus] " 가입 신청"]]
               (signup-form req)]]]]]
          [:div.container
           [:div.row
            [:div.page-header [:h1 "Git 저장소 서비스"]]
            [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
            [:div.page-header [:h1 "위키 페이지 작성"]]]]))

(defn dashboard [req]
  (layout req {:title (brand-name+ "대시보드")}
          [:div.container
           [:div.row
            [:div.page-header [:h1 "Git 저장소 서비스"]]
            [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
            [:div.page-header [:h1 "위키 페이지 작성"]]]]

          [:div.container
           [:div.row
            [:div.page-header [:h1 "Git 저장소 서비스"]]
            [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
            [:div.page-header [:h1 "위키 페이지 작성"]]]]))

(defn login-form [req]
  (letfn [(fg [label-text & input-section]
            [:div.form-group
             [:label.control-label.col-sm-3 label-text]
             [:div.col-sm-9 input-section]])]
    [:form#login-form.form-horizontal {:method "POST" :action "/user/login"}
     (fg "아이디" [:input.form-control
                   {:type "text" :name "userid" :autofocus true}])
     (fg "비밀번호" [:input.form-control
                     {:type "password" :v-model "password" :name "password" :placeholder "********"}])
     (fg "" [:button.btn.btn-primary "로그인"])
     (anti-forgery-field)]))

(defn login-page [req]
  "로그인 입력 창"
  (layout req {:title (brand-name+ "> 로그인")}
          [:div.container.narrow-container
           [:div.page-header
            [:h2 [:i.fa.fa-sign-in] " 로그인 "]]
           [:div.row
            [:div.col-sm-12
             [:div.panel.panel-ohucode [:div.panel-body (login-form req)]]]]]))
