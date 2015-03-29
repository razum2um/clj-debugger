(ns debugger.main
  (:require [debugger.core :refer :all])
  (:gen-class))

;; `lein run -m debugger.main 1 2` shouldn't stop in debugger
(defn -main [& args]
  (println "-main:"
           (break (pr-str args))))

