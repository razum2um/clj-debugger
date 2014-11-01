(ns debugger.core
  (:require clojure.reflect))

(declare ^:dynamic *locals*)

(defmacro dbg
  [x]
  `(let [x# ~x]
     (do
       (println '~x "->" x#)
       x#)))

(defn prompt-fn []
  (printf "break %s=> " (ns-name *ns*)))

(defn print-table
  ([ks rows]
     (when (seq rows)
       (let [widths (map
                     (fn [k]
                       (apply max (count (str k)) (map #(count (str (get % k))) rows)))
                     ks)
             spacers (map #(apply str (repeat % "-")) widths)
             fmts (map #(str "%-" % "s") widths)
             fmt-row (fn [leader divider trailer row]
                       (str leader
                            (apply str (interpose divider
                                                  (for [[col fmt] (map vector (map #(get row %) ks) fmts)]
                                                    (format fmt (str col)))))
                            trailer))]
         (println)
         (doseq [row rows]
           (println (fmt-row " " " " " " row))))))
  ([rows] (print-table (keys (first rows)) rows)))

(defn help-message []
  (print-table
    [:cmd :help]
    [{:cmd "(h)" :help "prints this help"}
     {:cmd "(w)" :help "prints code around breakpoint"}
     {:cmd "(l)" :help "prints locals"}
     {:cmd "(c)" :help "continues execution and preserves the result"}
     {:cmd "^D"  :help "type Ctrl-D to exit break-repl and pass last result further"}
     {:cmd ""    :help ""}
     {:cmd ""    :help "you can also access locals directly and build sexp with them"}
     {:cmd ""    :help "any last execution result (but `nil`) before exit will be passed further"}
     {:cmd ""    :help "if last result is `nil` execution will continue normally"}]))

(defn eval-fn [return-val cached-cont-val locals-fn cont-fn source form]
  (do
    (case (clojure.string/trim (str form))
      "(h)" (do
              (help-message)
              (println))
      "(w)" (do
              (println)
              (println source "\n"))
      "(l)" (do
              (println (locals-fn)))
      "(c)" (do
              (reset! cached-cont-val (cont-fn)))
      (do
        (reset!
          return-val
          (binding [*locals* (locals-fn)]
            (eval
              `(let ~(vec (mapcat #(list % `(*locals* '~%)) (keys *locals*)))
                 ~form))))))))

;; TODO use readline/jline
(defn read-fn [request-prompt request-exit]
  ;; (println "> Read-fn with" (pr-str request-prompt) "and" (pr-str request-exit))
  (or ({:line-start request-prompt :stream-end request-exit}
       (clojure.main/skip-whitespace *in*))
      (let [input (read)]
        (clojure.main/skip-if-eol *in*)
        input)))

(defn caught-fn [e]
  (println "> Start caught-fn")
  (let [ex (clojure.main/repl-exception e)
        tr (.getStackTrace ex)
        el (when-not (zero? (count tr)) (aget tr 0))]
    (binding [*out* *err*]
      (println (str (-> ex class .getSimpleName)
                    " " (.getMessage ex) " "
                    (when-not (instance? clojure.lang.Compiler$CompilerException ex)
                      (str " " (if el (clojure.main/stack-element-str el) "[trace missing]"))))))))


(defn- unmangle [s]
  (clojure.string/replace s #"^(.+)\$(.+)\W?.*$" (fn [[_ ns-name fn-name]] (str ns-name "/" fn-name))))

(defn- format-line-with-line-numbers [macro-line line line-number]
  (if (= macro-line line-number)
    (str "=> " line-number ": " line)
    (str "   " line-number ": " line)))

(defmacro break [& body]
  (let [env (into {} (map (fn [[sym bind]] [`(quote ~sym) (.sym bind)]) &env))
        ;; _ (println "form=" &form "meta" (meta &form))
        macro-line (:line (meta &form))]
    `(let [return-val# (atom nil)
           cached-cont-val# (atom nil)
           cont-fn# #(identity ~@body)
           locals-fn# #(identity ~env)

           path-to-src# (-> (java.io.File. ".") .getCanonicalPath)
           outer-fn-name# (-> (Throwable.) .getStackTrace first .getClassName unmangle symbol)
           outer-fn-meta# (-> outer-fn-name# find-var meta)
           outer-fn-line# (:line outer-fn-meta#)
           outer-fn-path# (and outer-fn-meta# (str path-to-src# "/src/" (:file outer-fn-meta#) ":" outer-fn-line#))

           outer-fn-source# (clojure.string/trim (clojure.repl/source-fn outer-fn-name#))
           source# (clojure.string/join
                     "\n"
                     (mapv (partial format-line-with-line-numbers ~macro-line)
                           (or (clojure.string/split outer-fn-source# #"\n") [])
                           (map (partial + outer-fn-line#) (range))))

           macro-eval-fn# (partial eval-fn return-val# cached-cont-val# locals-fn# cont-fn# source#)]
       (println "\nBreak from:" outer-fn-path# "(type \"(h)\" for help)\n")
       (println source# "\n")
       (clojure.main/repl
         :prompt prompt-fn
         :eval macro-eval-fn#
         :read read-fn
         :caught caught-fn)
       (or
         (deref return-val#)
         (deref cached-cont-val#)
         (cont-fn#)))))

(defn foo [& args]
  (let [x "world"
        y '(1 2)
        z (Object.)
        ret (break (inc 42))]
    (println "Exit foo with" ret)))

(defn bar [multi]
  (let [my-fn (dbg (break (fn inner [x] (* multi x))))]
    (map my-fn (range 2))))

(defn qux [multi]
  (let [my-fn (fn inner [x] (break (* multi x)))]
    (map my-fn (range 2))))

