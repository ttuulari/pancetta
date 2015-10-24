(ns pancetta.streams
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! alts! timeout close! pipe]]
            [cljs.nodejs :as node]
            [cljs.core.match :refer-macros [match]]
            [pancetta.time :as t])) 

(node/enable-util-print!)

(defn sample
  "Periodically sample in channel. Returns out channel that gets sampled values."
  ([in ms]
   (sample in ms (chan)))
  ([in ms out]
   (go-loop
     [] 
     (let [timer  (timeout ms)]
       (match (alts! [in timer])
              [nil in]  (close! out)
              [v in]    (do
                          (>! out v)
                          (<! (timeout ms))
                          (recur))
              [_ timer] (recur))))
   out))

(defn debounce
  "Mimics jQuery debounce. Returns out channel with debounced values."
  ([in ms]
   (debounce in ms (chan)))
  ([in ms out]
   (go-loop [value nil]
            (let [t (timeout ms)
                  s (t/epoch)]
              (match [(alts! [in t]) value]
                     ; New value in
                     [[(v :guard some?) in] _] (recur v)
                     ; Timeout: no value in yet
                     [[_ t] nil]               (recur nil)
                     ; Timeout: value in, send to out and reset latest value
                     [[_ t] value]             (do
                                                 (>! out value)
                                                 (recur nil))
                     ; In channel closed
                     [[nil in] _] (if (nil? value)
                                    ; In channel closed and no current value
                                    (close! out)
                                    ; In channel closed.
                                    ; Wait remaining time and send value out.
                                    (let [diff (- ms
                                                  (- (t/epoch) s))]
                                      (<! (timeout diff))
                                      (>! out value)
                                      (close! out))))))
   out))

(defn pipe-trans
  "Pipe values from in channel through transducer xf. Returns piped out channel."
  [in xf]
  (let [out (chan 1 xf)]
    (pipe in out)
    out))

(defn consume [c f]
  (go (loop []
        (when-some [v (<! c)]
          (f v)
          (recur)))))
