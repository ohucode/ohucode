(ns 오후코드.핸들러-깃-test
  (:use [clojure.test]
        [오후코드.핸들러-깃])
  (:require [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [오후코드.핸들러 :refer [앱라우트]]))

(deftest git-http-route
  (testing "/info/refs should response with a proper content-type"
    (let [응답 (앱라우트 (mock/request :get "/test/fixture/info/refs?service=git-upload-pack"))]
      (is (= (get-in 응답 [:headers "Content-Type"])
             "application/x-git-upload-pack-advertisement"))
      (is (= (:status 응답) 200)))
    (let [응답 (앱라우트 (mock/request :get "/test/fixture/info/refs?service=git-receive-pack"))]
      (is (= (get-in 응답 [:headers "Content-Type"])
             "application/x-git-receive-pack-advertisement"))))

  ;; 요청 파일 업데이트 필요함
  (comment 검사 "POST /git-upload-pack"
           (let [file (io/as-file "fixture/upload-pack-req.body")
                 요청 (merge
                       (mock/request :post "/u/p/git-upload-pack")
                       {:headers {"Content-Length" (.length file)
                                  "Content-Type" "application/x-git-upload-pack-request"
                                  "Accept" "application/x-git-upload-pack-result"
                                  "Content-Encoding" "gzip"
                                  "Accept-Encoding" "gzip"}
                        :body (io/input-stream file)})
                 응답 (앱라우트 요청)]
             (is (= (:status 응답) 200))
             (are [key value] (= (get-in 응답 [:headers key]) value)
               "Content-Type" "application/x-git-upload-pack-result"
               "Content-Encoding" "gzip"
               "Cache-Control" "no-cache, max-age=0, must-revalidate")))

  ;; 요청 파일 업데이트 필요함
  (comment 검사 "POST /git-receive-pack"
           (let [create-branch (io/as-file "fixture/receive-pack-create-branch.body")
                 delete-branch (io/as-file "fixture/receive-pack-delete-branch.body")]
             (doseq [fixture-file [create-branch delete-branch]]
               (let [요청 (merge
                           (mock/request :post "/u/p/git-receive-pack")
                           {:headers {"Content-Length" (.length fixture-file)
                                      "Content-Type" "application/x-git-receive-pack-request"
                                      "Accept" "application/x-git-receive-pack-result"
                                      "Accept-Encoding" "gzip"}
                            :body (io/input-stream fixture-file)})
                     응답 (앱라우트 요청)]
                 (is (= (:status 응답) 200))
                 (are [key value] (= (get-in 응답 [:headers key]) value)
                   "Content-Type" "application/x-git-receive-pack-result"
                   "Content-Encoding" "gzip"
                   "Cache-Control" "no-cache, max-age=0, must-revalidate"))))))
