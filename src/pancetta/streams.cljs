(ns pancetta.streams
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! alts! timeout close!]]
            [cljs.nodejs :as node]
            [cljs.core.match :refer-macros [match]]
            [pancetta.time :as t])) 

(node/enable-util-print!)

(defn sample
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
  ([ms in]
   (debounce ms (chan) in))
  ([ms out in]
   (go-loop [latest nil]
            (let [t (timeout ms)
                  s (t/epoch)]
              (match [(alts! [in t]) latest in]
                     [[_ t] nil _]  (recur nil)
                     [[_ t] latest _]    (do  ;timeout, stuff in
                                             (println "c2")
                                             (>! out latest)
                                             (recur nil))
                     [[(v :guard some?) in] _ _]   (do 
                                                     (println "c3" v)
                                                     (recur v))
                     [[nil in] _ _] (if (nil? latest)
                                      (close! out)
                                      (let [diff (- ms
                                                    (- (t/epoch) s))]
                                        (<! (timeout diff))
                                        (>! out latest)
                                        (close! out))))))
   out))

#_(defn debounce
  ([ms in]
   (debounce ms (chan) in))
  ([ms out in]
   (go
     (loop [timeout nil]
       (let [loc (<! in)]
         (println loc)
         (when timeout
           (js/clearTimeout timeout))
         (let [t (js/setTimeout #(go (>! out loc))
                                ms)]
           (recur t)))))
   out))

(defn consume [f c]
  (go (loop []
        (when-some [v (<! c)]
          (f v)
          (recur)))))
