(ns pancetta.streams
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! >! alts! timeout]]
            [clojure.core.match :refer [match]])) 

(defn debounce [in ms]
  (let [out (chan)]
    (go-loop [last-val nil]
      (let [val (if (nil? last-val) (<! in) last-val)
            timer (timeout ms)
            [new-val ch] (alts! [in timer])]
        (condp = ch
          timer (do (>! out val) (recur nil))
          in (recur new-val))))
    out))
