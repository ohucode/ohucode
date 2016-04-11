(defproject ohucode "0.0.1"
  :description "오후코드 실험버전"
  :url "https://github.com/ohucode/ohucode"
  :min-lein-version "2.5.3"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [cider/cider-nrepl "0.11.0-SNAPSHOT"]
                 [org.clojure/tools.namespace "0.2.11"]

                 [org.clojure/data.json "0.2.6"]
                 [org.javassist/javassist "3.20.0-GA"]
                 [misaeng "0.1.0"]

                 [compojure "1.5.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-logger-timbre "0.7.4"]
                 [aleph "0.4.1-beta5"]
                 [hiccup "1.0.5"]
                 [korma "0.4.2"]
                 [ragtime "0.5.2"]
                 [prone "0.8.2"]
                 [com.taoensso/timbre "4.2.1"]
                 [org.slf4j/slf4j-api "1.7.14"]
                 [com.fzakaria/slf4j-timbre "0.3.0"]

                 [org.postgresql/postgresql "9.4-1203-jdbc42"]
                 [org.eclipse.jgit/org.eclipse.jgit "4.2.0.201601211800-r"]
                 [amazonica "0.3.49"]

                 [org.clojure/clojurescript "1.7.228"]
                 [com.cemerick/piggieback "0.2.1"]
                 [figwheel-sidecar "0.5.0-6"]

                 [reagent "0.6.0-alpha"]
                 [re-frame "0.7.0-alpha-3"]
                 [secretary "1.2.3"]
                 [cljs-ajax "0.5.4"]
                 [org.apache.httpcomponents/httpclient "4.5.2"] ; cljs-ajax가 clj에서 리로드될 때 필요.
                 [cljsjs/bootstrap "3.3.6-0"]
                 [cljsjs/jquery "2.1.4-0"]
                 [cljsjs/marked "0.3.5-0"]
                 [cljsjs/highlight "8.4-0"]
                 [cljsjs/d3 "3.5.7-1"]
                 [cljsjs/google-analytics "2015.04.13-0"]]

  :source-paths ["src"]
  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]
            [lein-codox "0.9.4"]]
  :main 오후코드.서버/시작!
  ;; :hooks [leiningen.cljsbuild]
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]]}}
  :repl-options {:init-ns user
                 :init (set! *print-length* 50)
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :figwheel {:css-dirs ["resources/public/css"]
             :open-file-command "emacsclient"}
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src-cljs"]
                :figwheel {:on-jsload "ohucode.main/fig-reload"}
                :compiler {:main "ohucode.main"
                           :optimizations :none
                           :asset-path "js"
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js"}}
               {:id "min"
                :source-paths ["src-cljs"]
                :compiler {:main "ohucode.main"
                           :optimizations :advanced
                           :pretty-print false
                           :output-to "resources/public/js/main.js"}}]})
