(defproject debugger "0.2.1"
  :description "Debugger for Clojure"
  :url "https://github.com/razum2um/clj-debugger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :aliases {"test-all" ["with-profile" "+1.7:+1.8:+1.9" "test"]}
  :profiles {:dev {:dependencies [[eftest "0.5.2"]]}
             :test {:dependencies [[pjstadig/humane-test-output "0.8.3"]]
                    :injections [(require '[eftest.runner :refer [find-tests run-tests]])
                                 (require 'pjstadig.humane-test-output)
                                 (pjstadig.humane-test-output/activate!)]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}})
