(ns pancetta.streams
  (:require [clojure.core.async :as async :refer [go go-loop chan <! >! alts! timeout close! pipe]]
            [clojure.core.match :refer-macros [match]]
            [pancetta.time :as t])) 

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

(defn delayed
  "Delay events by ms. Returns out channel with delayed events."
  ([in ms]
   (delayed in ms (chan) (chan)))
  ([in ms out silent]
   (go-loop
     [to     (chan)
      q      []
      closed false
      i      in]
     (match
       [(alts! [i to]) q closed]
       ; First value received from in, start a timer of ms
       [[(v :guard some?) i] [] _]  (recur (timeout ms)
                                           (conj q {:stamp (+ (t/epoch) ms)
                                                    :value v})
                                           closed
                                           i)
       ; Value received from in, don't start a timer
       [[(v :guard some?) i] _ _]   (recur to
                                           (conj q {:stamp (+ (t/epoch) ms)
                                                    :value v})
                                           closed
                                           i)
       ; Input closed
       [[nil i] _ false]            (recur to q true silent)
       ; Timeout, input closed, and queue empty: close out
       [[_ to] [] true]             (close! out)
       ; Timout, values in queue, send to out
       [[_ to] q _]                 (let [[f s & t] q]
                                      (>! out (:value f))
                                      (if s
                                        ; Second value in queue present, start timer
                                        (recur (timeout (- (:stamp s)
                                                           (t/epoch)))
                                               (vec (rest q))
                                               closed
                                               i)
                                        ; No second value, no timer
                                        (recur silent 
                                               (vec (rest q))
                                               closed
                                               i)))))
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
