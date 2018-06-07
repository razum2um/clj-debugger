(ns debugger.debug-args-test
  (:require [debugger.debug-args :refer [dbg-defn dbg-fn]]
            [clojure.test :refer [deftest testing is]]))

(deftest dbg-defn-test
  (testing "inserts println statement into single arity"
    (is (= '(defn debugger.debug-args-test/f1
              [debugger.debug-args-test/arg1 debugger.debug-args-test/arg2]
              (println {"debugger.debug-args-test/arg1" debugger.debug-args-test/arg1,
                        "debugger.debug-args-test/arg2" debugger.debug-args-test/arg2})
              [debugger.debug-args-test/arg1 debugger.debug-args-test/arg2])
           (macroexpand-1 `(dbg-defn f1 [arg1 arg2] [arg1 arg2]))))))

(deftest dbg-defn-multiarity-test
  (testing "inserts println statement into multiple arity"
    (is (= '(defn debugger.debug-args-test/f2
              ([debugger.debug-args-test/arg1]
               (println {"debugger.debug-args-test/arg1" debugger.debug-args-test/arg1})
               [debugger.debug-args-test/arg1])
              ([debugger.debug-args-test/arg1 debugger.debug-args-test/arg2]
               (println {"debugger.debug-args-test/arg1" debugger.debug-args-test/arg1,
                         "debugger.debug-args-test/arg2" debugger.debug-args-test/arg2})
               [debugger.debug-args-test/arg1 debugger.debug-args-test/arg2]))
           (macroexpand-1 `(dbg-defn f2
                                     ([arg1] [arg1])
                                     ([arg1 arg2] [arg1 arg2])))))))

(deftest dbg-fn-test
  (testing "inserts println statement before args"
    (is (= '(fn [debugger.debug-args-test/arg1 debugger.debug-args-test/arg2]
              (println {"debugger.debug-args-test/arg1" debugger.debug-args-test/arg1,
                        "debugger.debug-args-test/arg2" debugger.debug-args-test/arg2})
              [debugger.debug-args-test/arg1 debugger.debug-args-test/arg2])
           (macroexpand-1 `(dbg-fn [arg1 arg2] [arg1 arg2]))))))
