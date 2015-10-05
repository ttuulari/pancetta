(ns pancetta.streams
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! >! alts! timeout close!]]
            [cljs.core.match :refer-macros [match]])) 

(defn debounce
  [in ms]
  (let [out (chan)]
    (go-loop
      [last-val nil]
      (let [timer (timeout ms)
            value (if (nil? last-val)
                    (<! in)
                    last-val)]
        (println "value" value)
        (if (nil? value)
          (close! out)
          (match (alts! [in timer])
                 [_ timer]     (do (println "timer")
                                   (>! out value)
                                   (recur nil))
                 [new-val _]   (recur new-val)))))
    out))

(defn seq->chan 
  [ms coll]
  (let [out (chan)]
    (go-loop
      [sequ values]
      (if (empty? sequ)
        (close! out)
        (do
          (<! (timeout ms))
          (println "out")
          (>! out (first sequ))
          (recur (rest sequ)))))
    out))

(defn consume
  [in fun]
  (go-loop
    []
    (let [value (<! in)]
      (if (nil? value)
        nil
        (do (fun value)
            (recur))))))
