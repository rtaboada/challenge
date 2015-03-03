(defproject challenge "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/test.check "0.7.0"]
                 [io.pedestal/pedestal.service "0.3.1"]
                 [io.pedestal/pedestal.jetty "0.3.1"]]

  :min-lein-version "2.0.0"
  :resource-paths ["config" "resources"]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "challenge.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.3.1"]]}}
  :main ^{:skip-aot true} challenge.server)
