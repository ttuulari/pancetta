(ns pancetta.streams
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! alts! timeout close!]]
            [cljs.nodejs :as node]
            [cljs.core.match :refer-macros [match]]
            [pancetta.time :as t])) 

(node/enable-util-print!)

(defn sample
  "Periodically sample in channel. Returns out channel that gets sampled values."
  ([ms in]
   (sample ms (chan) in))
  ([ms out in]
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
  ([ms in]
   (debounce ms (chan) in))
  ([ms out in]
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

(defn consume [f c]
  (go (loop []
        (when-some [v (<! c)]
          (f v)
          (recur)))))
