(ns pancetta.streams
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! alts! timeout close!]]
            [cljs.nodejs :as node]
            [cljs.core.match :refer-macros [match]])) 

(node/enable-util-print!)

(defn sample
  [ms in]
  (let [out (chan)]
    (go-loop
      [] 
      (<! (timeout ms))
      (let [timer  (timeout ms)]
        (match (alts! [in timer])
               [nil in]  (close! out)
               [v in]    (do
                           (>! out v)
                           (recur))
               [_ timer] (recur))))
    out))

(defn consume [f c]
  (go (loop []
        (when-some [v (<! c)]
          (f v)
          (recur)))))
