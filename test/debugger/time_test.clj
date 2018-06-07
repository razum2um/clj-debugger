(ns debugger.time-test
  (:import java.util.Date)
  (:require [debugger.time :as time]
            [clojure.test :refer :all]))

(def date (Date.))
(defmacro stubbing-time-deftest [name & body]
  `(deftest ~name
     (with-redefs [time/now (fn [] ~date)]
       (do ~@body))))

(stubbing-time-deftest now-test
  (is (= (.getTime date) (.getTime (time/now)))))


(stubbing-time-deftest minus-test
  (is (= (-> date .getTime (- 2000) (Date.) .getTime)
         (-> (time/now) (time/minus (time/seconds 2)) .getTime))))

(stubbing-time-deftest interval-test
  (is (= 4 (-> (time/now)
               (time/minus (time/seconds 4))
               (time/interval (time/now))
               (time/in-seconds)))))

(stubbing-time-deftest compatibility-test
  (are [last-quit-seconds-ago skip-repl-if-last-quit-ago check]
    (is (= check (->> (time/now) (time/interval (time/minus (time/now) (time/seconds last-quit-seconds-ago))) time/in-seconds (< skip-repl-if-last-quit-ago))))
    4 2 true
    3 2 true
    2 2 false
    1 2 false))
