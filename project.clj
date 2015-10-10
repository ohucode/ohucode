(defproject ohucode "0.0.1"
  :description "오후코드 실험버전"
  :url "https://github.com/ohucode/ohucode"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-logger-timbre "0.7.4"]
                 [aleph "0.4.0"]
                 [enlive "1.1.6"]
                 [korma "0.4.2"]
                 [crypto-password "0.1.3"]
                 [org.postgresql/postgresql "9.4-1203-jdbc42"]
                 [org.eclipse.jgit/org.eclipse.jgit "4.1.0.201509280440-r"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.23"]
                 [com.amazonaws/aws-java-sdk-ses "1.10.23"]
                 [com.amazonaws/aws-java-sdk-route53 "1.10.23"]]
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler ohucode.handler/app-dev}
  :main ohucode.server/start
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [org.clojure/tools.namespace "0.2.11"]]}})
