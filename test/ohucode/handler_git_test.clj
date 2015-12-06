(ns ohucode.handler-git-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [ohucode.handler :refer [app]]
            [ohucode.handler-git :refer :all]))

(deftest git-http-route
  (testing "/info/refs should response with a proper content-type"
    (let [res (app (mock/request :get "/u/p/info/refs?service=git-upload-pack"))]
      (is (= (get-in res [:headers "Content-Type"])
             "application/x-git-upload-pack-advertisement"))
      (is (= (:status res) 200)))
    (let [res (app (mock/request :get "/u/p/info/refs?service=git-receive-pack"))]
      (is (= (get-in res [:headers "Content-Type"])
             "application/x-git-receive-pack-advertisement"))))

  ;; 요청 파일 업데이트 필요함
  (comment testing "POST /git-upload-pack"
    (let [file (io/as-file "fixture/upload-pack-req.body")
          req (merge
               (mock/request :post "/u/p/git-upload-pack")
               {:headers {"Content-Length" (.length file)
                          "Content-Type" "application/x-git-upload-pack-request"
                          "Accept" "application/x-git-upload-pack-result"
                          "Content-Encoding" "gzip"
                          "Accept-Encoding" "gzip"}
                :body (io/input-stream file)})
          res (app req)]
      (is (= (:status res) 200))
      (are [key value] (= (get-in res [:headers key]) value)
        "Content-Type" "application/x-git-upload-pack-result"
        "Content-Encoding" "gzip"
        "Cache-Control" "no-cache, max-age=0, must-revalidate")))

  ;; 요청 파일 업데이트 필요함
  (comment testing "POST /git-receive-pack"
    (let [create-branch (io/as-file "fixture/receive-pack-create-branch.body")
          delete-branch (io/as-file "fixture/receive-pack-delete-branch.body")]
      (doseq [fixture-file [create-branch delete-branch]]
        (let [req (merge
                   (mock/request :post "/u/p/git-receive-pack")
                   {:headers {"Content-Length" (.length fixture-file)
                              "Content-Type" "application/x-git-receive-pack-request"
                              "Accept" "application/x-git-receive-pack-result"
                              "Accept-Encoding" "gzip"}
                    :body (io/input-stream fixture-file)})
              res (app req)]
          (is (= (:status res) 200))
          (are [key value] (= (get-in res [:headers key]) value)
            "Content-Type" "application/x-git-receive-pack-result"
            "Content-Encoding" "gzip"
            "Cache-Control" "no-cache, max-age=0, must-revalidate"))))))
