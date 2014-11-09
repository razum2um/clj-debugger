(ns debugger.config)

(declare ^:dynamic *locals*)
(def ^:dynamic *break-outside-repl* false)
(def ^:dynamic *skip* (atom {}))
(def ^:dynamic *code-context-lines* 5)

