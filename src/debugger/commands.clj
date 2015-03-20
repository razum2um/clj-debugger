(ns debugger.commands
  (:use debugger.config)
  (:require [debugger.config :refer :all]
            [debugger.formatter :refer [safe-find-var
                                        no-sources-found
                                        print-stack-table
                                        demunge
                                        map-numbered-source-lines
                                        print-table-left-align
                                        format-line-with-line-numbers]]))

(defn help-message []
  (print-table-left-align
    [:cmd :long :help]
    [{:cmd "(h)"  :long "(help)"      :help "prints this help"}
     {:cmd ""     :long "(wtf)"       :help "prints short stacktrace"}
     {:cmd ""     :long "(wtf??)"     :help "prints full stacktrace"}
     {:cmd ""     :long "(whereami)"  :help "prints full code of breakpointed function"}
     {:cmd "(l)"  :long "(locals)"    :help "prints locals"}
     {:cmd "(c)"  :long "(continue)"  :help "continues execution, preserves the result and will break here again"}
     {:cmd ""     :long "(skip 3)"    :help "skips next 3 breakpoints in this place"}
     {:cmd "(q)"  :long "(quit)"      :help "or type Ctrl-D to exit break-repl, pass last result further, will never break here anymore"}
     {:cmd ""     :long ""            :help ""}
     {:cmd ""     :long ""            :help "use (debugger.core/reset-skips!) if breaks are skipped"}
     {:cmd ""     :long ""            :help "you can also access locals directly and build sexp with them"}
     {:cmd ""     :long ""            :help "any last execution result (but `nil`) before exit will be passed further"}
     {:cmd ""     :long ""            :help "if last result is `nil` execution will continue normally"}]))

(defn print-full-source [break-line fn-symbol]
  (let [fn-meta (-> fn-symbol safe-find-var meta)
        format-fn (partial format-line-with-line-numbers false break-line)
        lines (map-numbered-source-lines format-fn fn-symbol)]
    (if (empty? lines)
      (println (no-sources-found fn-symbol))
      (do
        (println)
        (println (clojure.string/join "\n" lines) "\n")))))


(defn print-short-source [break-line fn-symbol]
  (let [fn-meta (-> fn-symbol safe-find-var meta)
        format-fn (partial format-line-with-line-numbers true break-line)
        lines (map-numbered-source-lines format-fn fn-symbol)]
    (cond
      (= 0 (count lines))
        (println (no-sources-found fn-symbol))
      (> (* 2 *code-context-lines*) (count lines))
        (print-full-source break-line fn-symbol)
      :else
        (do
          (println)
          (println (clojure.string/join "\n" (filter #(not (nil? %)) lines))
                   "\n")))))

(defn print-trace
  ([trace] (print-trace (constantly true) trace))
  ([filter-fn trace]
   (print-stack-table
     (->> trace
          seq
          (mapv (fn [i s] [i s]) (range))
          (filter filter-fn)
          (map (fn [[i s]] [(str "[" i "]")
                            (.getFileName s)
                            (.getLineNumber s)
                            (demunge (.getClassName s))]))))))
