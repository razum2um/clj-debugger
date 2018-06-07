(ns debugger.debug-args
  (:require [clojure.zip :as z]))

(def fn-decl? #{'defn 'fn})

(defn at-args? [loc]
  (and (-> loc z/node vector?)
       (or (some-> loc z/leftmost z/node fn-decl?)
           (some-> loc z/up z/leftmost z/node fn-decl?))))

(defn args-debug-inc
  ([args] (args-debug-inc 'println args))
  ([with args] (list with (into (sorted-map) (map (juxt str identity) args)))))

(defn debug-args [body]
  (loop [loc (z/seq-zip (seq body))]
    (if (z/end? loc)
      (z/root loc)
      (recur (z/next
              (cond (at-args? loc) (-> loc
                                       (z/insert-right (-> loc z/node args-debug-inc))
                                       z/rightmost)
                    :else loc))))))

(defmacro dbg-defn [& body] (debug-args (cons 'defn body)))
(defmacro dbg-fn [& body] (debug-args (cons 'fn body)))
