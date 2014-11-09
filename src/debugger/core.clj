(ns debugger.core
  (:require [leiningen.core.project :as lein]
            [debugger.config :refer :all]
            [debugger.formatter :refer [deanonimize-name
                                        demunge
                                        safe-find-var
                                        no-sources-found]]
            [debugger.commands :refer [print-short-source]]
            [debugger.repl :refer [prompt-fn read-fn eval-fn caught-fn]]))

(defn reset-skips! []
  (reset! *skip* {}))

(defmacro dbg
  [x]
  `(let [x# ~x]
     (do
       (println '~x "->" x#)
       x#)))

(defmacro break [& body]
  (let [env (into {} (map (fn [[sym bind]] [`(quote ~sym) (.sym bind)]) &env))
        break-line (:line (meta &form))]
    `(let [trace# (-> (Throwable.) .getStackTrace seq)
           outer-fn-symbol# (-> trace# first .getClassName demunge deanonimize-name symbol)
           repl?# (->> trace#
                       (map #(.getClassName %))
                       (some #(re-find #"\$read_eval_print_" %)))]

       (if (and (or *break-outside-repl* repl?#) (= 0 ((swap! *skip*
                                                               #(assoc % outer-fn-symbol#
                                                                       (dec (or (% outer-fn-symbol#) 1))))
                                                        outer-fn-symbol#)))
         (do
           (let [macro-break-line# (or (:break-line ~@body) ~break-line 1)
                 cont-fn# #(identity (or (:exception ~@body) ~@body))
                 locals-fn# #(identity (or (:env ~@body) ~env))

                 return-val# (atom nil)
                 cached-cont-val# (atom nil)
                 signal-val# (atom nil)

                 project-dir# (-> (java.io.File. ".") .getCanonicalPath)
                 project# (lein/read (str project-dir# "/project.clj"))
                 path-to-src# (or (first (:source-paths project#))
                                  (str project-dir# "/src"))
                 outer-fn-meta# (-> outer-fn-symbol# safe-find-var meta)
                 outer-fn-path# (if outer-fn-meta#
                                  (str path-to-src# "/" (:file outer-fn-meta#) ":" (:line outer-fn-meta#))
                                  (no-sources-found outer-fn-symbol#))

                 macro-ns# (ns-name (or (:ns outer-fn-meta#) *ns*))
                 macro-eval-fn# (partial eval-fn macro-ns# macro-break-line# outer-fn-symbol# project# trace# signal-val# return-val# cached-cont-val# locals-fn# cont-fn#)
                 macro-read-fn# (partial read-fn signal-val#)
                 macro-prompt-fn# (partial prompt-fn outer-fn-symbol# macro-break-line# signal-val#)]
             (println "\nBreak from:" outer-fn-path# "(type \"(help)\" for help)")
             (print-short-source macro-break-line# outer-fn-symbol#)
             (clojure.main/repl
               :prompt macro-prompt-fn#
               :eval macro-eval-fn#
               :read macro-read-fn#
               :caught caught-fn)
             (or
               (deref return-val#)
               (deref cached-cont-val#)
               (cont-fn#))))
         ;; not repl or skip
         (do ~@body)))))


(defmacro break-catch [& body]
  (let [env (into {} (map (fn [[sym bind]] [`(quote ~sym) (.sym bind)]) &env))
        break-line (:line (meta &form))]
  `(try
    (do ~@body)
    (catch Exception ~'e
      (break {:break-line ~break-line :env ~env :exception ~'e})))))

