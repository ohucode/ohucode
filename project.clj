(defproject ohucode "0.0.1"
  :description "오후코드 실험버전"
  :url "https://github.com/ohucode/ohucode"
  :min-lein-version "2.1.2"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/data.json "0.2.6"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-logger-timbre "0.7.4"]
                 [aleph "0.4.0"]
                 [hiccup "1.0.5"]
                 [korma "0.4.2"]
                 [ragtime "0.5.2"]
                 [prone "0.8.2"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.postgresql/postgresql "9.4-1203-jdbc42"]
                 [org.eclipse.jgit/org.eclipse.jgit "4.2.0.201601211800-r"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.23"]
                 [com.amazonaws/aws-java-sdk-ses "1.10.23"]
                 [com.amazonaws/aws-java-sdk-route53 "1.10.23"]
                 [misaeng "0.1.0"]]
  :plugins [[lein-figwheel "0.5.0-4"]]
  :ring {:handler 오후코드.핸들러/app-dev}
  :main 오후코드.서버/시작
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [org.clojure/tools.namespace "0.2.11"]]}}
  :repl-options {:init-ns user
                 :init (set! *print-length* 50)})
