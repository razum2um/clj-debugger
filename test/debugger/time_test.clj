(ns debugger.time-test
  (:import java.util.Date)
  (:require [debugger.time :refer :all]
            [clojure.test :refer :all]))

(deftest now-test
  (is (= (.getTime (Date.)) (.getTime (now)))))


(deftest minus-test
  (is (= (-> (Date.).getTime (- 2000) (Date.) .getTime)
         (-> (now) (minus (seconds 2)) .getTime))))

(deftest interval-test
  (is (= 4 (-> (now)
               (minus (seconds 4))
               (interval (now))
               (in-seconds)))))

(deftest compatibility-test
  (are [last-quit-seconds-ago skip-repl-if-last-quit-ago check]
    (is (= check (->> (now) (interval (minus (now) (seconds last-quit-seconds-ago))) in-seconds (< skip-repl-if-last-quit-ago))))
    4 2 true
    3 2 true
    2 2 false
    1 2 false))
