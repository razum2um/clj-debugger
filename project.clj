(defproject debugger "0.1.2-SNAPSHOT"
  :description "Debugger fro Clojure"
  :url "https://github.com/razum2um/clj-debugger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 ;; [org.clojure/clojure "1.7.0-master-SNAPSHOT"]
                 [lein-ubersource "0.1.1"]
                 [org.bitbucket.mstrobel/procyon-compilertools "0.5.27"]
                 ;; [slothcfg "1.0.1"]
                 [leiningen "2.5.0"]
                 ;; [org.clojars.razum2um/jd-core-java "1.2"]
                 ]
  ;; :plugins [[slothcfg "1.0.1"]]
  ;; :main ^:skip-aot debugger.main ;; breaks (refresh)
  )
