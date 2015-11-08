(ns pancetta.time)

(defn epoch []
  (quot (System/currentTimeMillis) 1000))
