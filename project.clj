(defproject debugger "0.1.4"
  :description "Debugger for Clojure"
  :url "https://github.com/razum2um/clj-debugger"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 ;; [org.clojure/clojure "1.7.0-master-SNAPSHOT"]
                 [clj-time "0.9.0"]
                 ;; [leiningen "2.5.0"]
                 ;; [lein-ubersource "0.1.1"]
                 ;; [org.bitbucket.mstrobel/procyon-compilertools "0.5.27"]
                 ;; [slothcfg "1.0.1"]
                 ;; [org.clojars.razum2um/jd-core-java "1.2"]
                 ]
  ;; :plugins [[slothcfg "1.0.1"]]
  ;; :main ^:skip-aot debugger.main ;; breaks (refresh)
  ;; :jvm-opts ["-Xdebug"
  ;;            "-Xrunjdwp:transport=dt_socket,address=8000,server=y"]
  )
