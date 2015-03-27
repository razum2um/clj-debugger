(ns debugger.formatter
  (:require [clojure.repl]
            [debugger.config :refer :all]))

(defn no-sources-found [fn-symbol]
  (str "No source found for " fn-symbol "\n"))

(defn print-borderless-table-with-alignment
  "Borrowed from clojure.pprint/print-table"
  ([spacers alignment-fns rows]
   (if (coll? (first rows)) ;; add this
     (print-borderless-table-with-alignment spacers alignment-fns (range (count (first rows))) rows)
     (print-borderless-table-with-alignment spacers alignment-fns (keys (first rows)) rows)))
  ([spacers alignment-fns ks rows]
     (when (seq rows)
       (let [widths (map
                     (fn [k]
                       (apply max (count (str k)) (map #(count (str (get % k))) rows)))
                     ks)
             ;; parametrize alignment
             fmts (mapv (fn [i w] ((nth (cycle alignment-fns) i) w)) (range) widths)
             fmt-row (fn [leader dividers trailer row]
                       (str leader
                            (apply str (butlast (interleave
                                         (for [[col fmt] (map vector (map #(get row %) ks) fmts)]
                                           (format fmt (str col)))
                                         (cycle dividers))))
                            trailer))]
         (println)
         ;; parametrize spacers
         (doseq [row rows]
           (println (fmt-row " " spacers "" row)))))))

(def print-table-left-align
  (partial
    print-borderless-table-with-alignment
    [\tab]
    [(fn [width] (str "%-" width "s"))]))

(def print-stack-table
  (partial
    print-borderless-table-with-alignment
    [\tab ":" \tab] ;; dividers = (count cols) - 1
    [(fn [width] (str "%" width "s"))
     (fn [width] (str "%" width "s"))
     (fn [width] (str "%-" width "s"))
     (fn [width] (str "%-" width "s"))]))

(defn format-line-with-line-numbers [short? break-line line-number line]
  {:pre [(not (nil? break-line))]}
  (cond
    (= break-line line-number) (str "=> " line-number ": " line)
    (and short? (<= line-number (- break-line *code-context-lines*))) nil
    (and short? (>= line-number (+ break-line *code-context-lines*))) nil
    :else (str "   " line-number ": " line)))

(defn deanonimize-name [^String s]
  "Inner qualified names `debugger.core-test/err/fn--4248` -> no source found"
  (clojure.string/join "/" (take 2 (clojure.string/split s #"/"))))


(defn safe-find-var [sym]
  "No raise of not found ns of symbol"
  (and (-> sym namespace symbol find-ns)
       (-> sym find-var)))


(defn map-numbered-source-lines [f fn-symbol]
  (let [fn-source (clojure.repl/source-fn fn-symbol)
        fn-source-lines (if fn-source (clojure.string/split-lines fn-source) [])
        fn-meta (-> fn-symbol safe-find-var meta)
        fn-source-start-line (or (:line fn-meta) 1)
        line-numbers (map (partial + fn-source-start-line) (range))]
    (mapv f line-numbers fn-source-lines)))


(defn fn-try [f & args]
  (if (not (nil? (first args)))
    (apply f args)
    nil))

(defn demunge [s]
  (let [[raw-ns-name fn-name & tail] (clojure.string/split s #"\$")
        demunged-ns-name (clojure.main/demunge raw-ns-name)]
    (if fn-name
      (clojure.string/join "/" [(or (fn-try ns-name (some (comp find-ns symbol) [raw-ns-name demunged-ns-name])) raw-ns-name)
                                (clojure.main/demunge fn-name)])
      s)))

(defn non-std-trace-element? [^StackTraceElement s]
  (nil? (re-find #"^clojure\.|^java\." (.getClassName s))))



