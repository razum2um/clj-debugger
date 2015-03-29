(ns debugger.core
  (:require [debugger.time :as t]
            [debugger.config :refer :all]
            [debugger.formatter :refer [deanonimize-name
                                        demunge
                                        safe-find-var
                                        no-sources-found]]
            [debugger.commands :refer [print-short-source print-trace]]
            [debugger.repl :refer [prompt-fn read-fn eval-fn caught-fn]])
  (:import [clojure.lang Compiler$LocalBinding]))

(defn- sanitize-env
  [env]
  (into {} (for [[sym bind] env
                 :when (instance? Compiler$LocalBinding bind)]
             [`(quote ~sym) (.sym bind)])))

(defmacro dbg
  [x]
  `(let [x# ~x]
     (do
       (println '~x "->" x#)
       x#)))

(defmacro break [& body]
  (let [env (sanitize-env &env)
        break-line (:line (meta &form))]
    `(let [trace# (-> (Throwable.) .getStackTrace seq)
           outer-fn-symbol# (-> trace# first .getClassName demunge deanonimize-name symbol)
           repl?# (->> trace#
                       (map #(.getClassName %))
                       (some #(re-find #"\$read_eval_print_" %)))]

       (if (and (or *break-outside-repl* repl?#)
                (->> (t/now) (t/interval @*last-quit-at*) t/in-seconds (< *skip-repl-if-last-quit-ago*)))
         (do
           (when-let [e# (:exception ~@body)]
             (-> e# .getMessage println)
             (-> e# .getStackTrace print-trace))
           (let [macro-break-line# (or (:break-line ~@body) ~break-line 1)
                 cont-fn# #(identity (or (:exception ~@body) ~@body))
                 locals-fn# #(identity (or (:env ~@body) ~env))

                 return-val# (atom nil)
                 cached-cont-val# (atom nil)
                 signal-val# (atom nil)

                 project-dir# (-> (java.io.File. ".") .getCanonicalPath)
                 path-to-src# (str project-dir# "/src")
                 outer-fn-meta# (-> outer-fn-symbol# safe-find-var meta)
                 outer-fn-path# (if outer-fn-meta#
                                  (str path-to-src# "/" (:file outer-fn-meta#) ":" (:line outer-fn-meta#))
                                  (no-sources-found outer-fn-symbol#))

                 macro-ns# (ns-name (or (:ns outer-fn-meta#) *ns*))
                 macro-eval-fn# (partial eval-fn macro-ns# macro-break-line# outer-fn-symbol# trace# signal-val# return-val# cached-cont-val# locals-fn# cont-fn#)
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
  (let [env (sanitize-env &env)
        break-line (:line (meta &form))]
  `(try
    (do ~@body)
    (catch Throwable ~'e
      (break {:break-line ~break-line :env ~env :exception ~'e})))))

