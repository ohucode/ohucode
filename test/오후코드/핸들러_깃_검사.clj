(ns 오후코드.핸들러-깃-검사
  (:use [미생.기본]
        [미생.검사]
        [오후코드.핸들러-깃])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [오후코드.핸들러 :refer [앱라우트]]))

(검사정의 git-http-route
  (검사 "/info/refs should response with a proper content-type"
    (가정 [응답 (앱라우트 (mock/request :get "/u/p/info/refs?service=git-upload-pack"))]
      (확인 (= (get-in 응답 [:headers "Content-Type"])
               "application/x-git-upload-pack-advertisement"))
      (확인 (= (:status 응답) 200)))
    (가정 [응답 (앱라우트 (mock/request :get "/u/p/info/refs?service=git-receive-pack"))]
      (확인 (= (get-in 응답 [:headers "Content-Type"])
               "application/x-git-receive-pack-advertisement"))))

  ;; 요청 파일 업데이트 필요함
  (comment 검사 "POST /git-upload-pack"
    (가정 [file (io/as-file "fixture/upload-pack-req.body")
           요청 (merge
                 (mock/request :post "/u/p/git-upload-pack")
                 {:headers {"Content-Length" (.length file)
                            "Content-Type" "application/x-git-upload-pack-request"
                            "Accept" "application/x-git-upload-pack-result"
                            "Content-Encoding" "gzip"
                            "Accept-Encoding" "gzip"}
                  :body (io/input-stream file)})
           응답 (앱라우트 요청)]
      (확인 (= (:status 응답) 200))
      (확인* [key value] (= (get-in 응답 [:headers key]) value)
             "Content-Type" "application/x-git-upload-pack-result"
             "Content-Encoding" "gzip"
             "Cache-Control" "no-cache, max-age=0, must-revalidate")))

  ;; 요청 파일 업데이트 필요함
  (comment 검사 "POST /git-receive-pack"
    (가정 [create-branch (io/as-file "fixture/receive-pack-create-branch.body")
           delete-branch (io/as-file "fixture/receive-pack-delete-branch.body")]
      (doseq [fixture-file [create-branch delete-branch]]
        (가정 [요청 (merge
                     (mock/request :post "/u/p/git-receive-pack")
                     {:headers {"Content-Length" (.length fixture-file)
                                "Content-Type" "application/x-git-receive-pack-request"
                                "Accept" "application/x-git-receive-pack-result"
                                "Accept-Encoding" "gzip"}
                      :body (io/input-stream fixture-file)})
               응답 (앱라우트 요청)]
          (확인 (= (:status 응답) 200))
          (확인* [key value] (= (get-in 응답 [:headers key]) value)
            "Content-Type" "application/x-git-receive-pack-result"
            "Content-Encoding" "gzip"
            "Cache-Control" "no-cache, max-age=0, must-revalidate"))))))
