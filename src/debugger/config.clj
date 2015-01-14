(ns debugger.config
  ;; (:require [leiningen.core.project :as lein])
  )

(declare ^:dynamic *locals*)
(def ^:dynamic *break-outside-repl* false)
(def ^:dynamic *code-context-lines* 5)
(def ^:dynamic *locals-print-length* 10)

(defn- project* [] (lein/read (str (-> (java.io.File. ".") .getCanonicalPath) "/project.clj")))
(def project (memoize project*))

