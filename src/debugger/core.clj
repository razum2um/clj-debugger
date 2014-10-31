(ns debugger.core)

(declare ^:dynamic *locals*)

(defmacro dbg
  [x]
  `(let [x# ~x]
     (do
       (println '~x "->" x#)
       x#)))

(defn prompt-fn []
  ;; (printf "%s=> " (ns-name *ns*))
  )

(defmacro local-bindings []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym)) symbols) symbols)))

(defn eval-with-locals [locals form]
  (binding [*locals* locals]
    (eval
      `(let ~(vec (mapcat #(list % `(*locals* '~%)) (keys locals)))
         ~form))))

(defn eval-fn [cont-fn form]
  (do
    (println "> Start eval-fn")
    (case (clojure.string/trim (str form))
      "(c)" (do (println "> Eval-fn continues")
                      (cont-fn))
      ;; "(e)" (do (println "> Eval-fn environment")
      ;;                 *locals*)
      (do
        (println "> Eval-fn got" (pr-str form))
        (jeval form)))))

(defn read-fn [request-prompt request-exit]
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



(defmacro break [& body]
  (binding [
        ;environment (vec (map (fn [sym] `(quote ~sym)) (keys &env)))
        *locals* (local-bindings)
        ]
    (println "> *locals*" *locals*)
    ;; (println "> Env:" environment)
    `(let [cont-fn# #(~body)
           eval-with-cont-fn# (partial eval-fn cont-fn#)
           ;; s# (println "> x" (class x))
           ]
       (clojure.main/repl
         :prompt prompt-fn
         :eval eval-with-cont-fn#
         :read read-fn
         :caught caught-fn))))

(defn foo [& args]
  (let [x "world"]
    (break
      (println "hello," x)
      (dbg (+ 1 2)))
    (println "Exit foo")))

