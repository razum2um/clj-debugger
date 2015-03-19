(ns debugger.time
  (:import java.util.Date
           java.util.concurrent.TimeUnit))

(defn now []
  (Date.))

(defn seconds [^Integer n]
  (.toMillis (TimeUnit/SECONDS) n))

(defn in-seconds [n]
  (.toSeconds (TimeUnit/MILLISECONDS) n))

(defn interval [^Date a ^Date b]
  (- (.getTime b) (.getTime a)))

(defn minus [^Date t period]
  (Date. (- (.getTime t) period)))
