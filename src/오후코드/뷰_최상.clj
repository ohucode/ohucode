(ns 오후코드.뷰-최상
  (:use [미생.기본]
        [미생.한글]
        [오후코드.기본]
        [오후코드.뷰]
        [오후코드.뷰-가입]
        [hiccup.core]
        [hiccup.page]))

(함수 basic-content [요청 제목 & 본문]
  (레이아웃 요청 {:title (서비스명+ "> " 제목)}
            [:div.container [:div.row [:h1 제목] 본문]]))

(함수 not-found [요청]
  {:status 404
   :body (basic-content 요청 "찾을 수 없습니다"
                        [:p "요청하신 페이지를 찾을 수 없습니다."])})

(함수 미구현 [요청]
  {:status 404
   :body (basic-content 요청 "아직 구현하지 못한 기능입니다."
                        [:p "요청하신 페이지를 찾을 수 없습니다."])})

(함수 요청에러 [요청 메시지]
  {:status 403
   :body (basic-content 요청 "입력 값 오류"
                        [:p 메시지])})

(함수 terms-of-service [요청]
  (basic-content 요청 "서비스 이용약관"
                 [:p "오후코드 서비스를 이용하면..."]))

(함수 privacy-policy [요청]
  (basic-content 요청 "개인정보 보호정책"
                 [:p "오후코드 서비스를 이용하면..."]))

(함수 credits [요청]
  (basic-content 요청 "감사의 말"
                 [:div {:data-markdown true} (slurp "CREDITS.md")]))

(함수 intro-guest [요청]
  (레이아웃 요청 {:title (서비스명+ "첫화면")}
            [:div.jumbotron
             [:div.row
              [:div.col-xs-6.col-md-8
               [:h1 서비스명]
               [:p "즐겁고 효율적인 프로그래밍의 동반자, " 서비스명 "에 오신 것을 환영합니다. "
                (은|는 서비스명) " 여러분의 프로젝트에 꼭 필요한 소스코드 저장소(Git 리모트 리포지토리)를 "
                "편리하게 제공합니다."]]
              [:div.col-xs-6.col-md-4
               [:div.panel.panel-signup
                [:div.panel-body
                 [:div.page-header [:h4 [:i.fa.fa-user-plus] " 가입 신청"]]
                 [:div#signup-form]]]]]]
            [:div.container
             [:div.row
              [:div.page-header [:h1 "Git 저장소 서비스"]]
              [:div.page-header [:h1 "프로젝트 구성원 권한 관리"]]
              [:div.page-header [:h1 "위키 페이지 작성"]]]]))

(함수 대시보드 [요청]
  (레이아웃 요청 {:title (서비스명+ "대시보드")}
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

(함수 로그인양식 [요청]
  (가정함 [(fg [라벨 & 입력부]
               [:div.form-group
                [:label.control-label.col-sm-3 라벨]
                [:div.col-sm-9 입력부]])]
    [:form#login-form.form-horizontal {:method "POST" :action "/user/login"}
     (fg "아이디" [:input.form-control
                   {:type "text" :name "userid" :autofocus true}])
     (fg "비밀번호" [:input.form-control
                     {:type "password" :v-model "password" :name "password" :placeholder "********"}])
     (fg "" [:button.btn.btn-primary "로그인"])
     (anti-forgery-field)]))

(함수 로그인-페이지 [요청]
  "로그인 입력 창"
  (레이아웃 요청 {:title (서비스명+ "> 로그인")}
            [:div.container.narrow-container
             [:div.page-header
              [:h2 [:i.fa.fa-sign-in] " 로그인 "]]
             [:div.row
              [:div.col-sm-12
               [:div.panel.panel-ohucode [:div.panel-body (로그인양식 요청)]]]]]))
