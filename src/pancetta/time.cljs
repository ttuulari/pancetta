(ns pancetta.time)

(defn epoch []
  (.getTime (js/Date.)))
