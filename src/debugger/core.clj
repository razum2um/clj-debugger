(ns debugger.core
  (:require clojure.reflect))

(declare ^:dynamic *locals*)

(defmacro dbg
  [x]
  `(let [x# ~x]
     (do
       (println '~x "->" x#)
       x#)))

(defn public-inspect [x]
  (clojure.pprint/print-table
    (filter #(contains? (:flags %) :public)
            (map
              #(apply dissoc % [:exception-types])
              (:members (clojure.reflect/reflect x))))))

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

(defn eval-fn [env-keys env-vals cont-fn form]
  ;; (binding [*locals* locals]
  ;;   (eval
  (do
    (println "> Start eval-fn")
    (case (clojure.string/trim (str form))
      "(c)" (do (println "> Eval-fn continues")
                      (cont-fn))
      "(e)" (do (println "> Eval-fn env-keys")
                      env-keys)
      "(v)" (do (println "> Eval-fn env-vals")
                env-vals)
      (do
        (println "> Eval-fn got" (pr-str form))
        (jeval form)))))
  ;; ))

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
  (let [
        ;; environment (vec (map (fn [sym] `(quote ~sym)) (keys &env)))
        env-keys (vec (mapcat (fn [sym] `[(quote ~sym)]) (keys &env)))
        ;; environment (local-bindings)
        fval (.sym (first (vals &env)))
        ]
    ;; (println "> local-bindings:" )
    ;; (println "> Env keys:" env-keys)
    `(let [cont-fn# #(~body)
           env-vals# ~fval
           ;; a# (println "> Env pre fn vals:" x)
           eval-with-cont-fn# (partial eval-fn ~env-keys env-vals# cont-fn#)
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

