(defproject codohu "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [enlive "1.1.6"]
                 [org.eclipse.jgit/org.eclipse.jgit "4.0.2.201509141540-r"]]
  :plugins [[lein-ring "0.9.6"]]
  :ring {:handler codohu.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
