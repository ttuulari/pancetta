(ns pancetta.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as async :refer [chan <! >! alts! timeout]]
            [pancetta.streams :as s]))

(node/enable-util-print!)

(defn test-sampling
  []
  (let [c       (async/chan)
        _       (async/onto-chan c (range 5))
        sampled (s/sample 1000 c)]
    (s/consume println sampled)))

(defn test-debounce
  []
  (let [c       (async/chan)
        _       (async/onto-chan c (range 5))
        sampled (s/sample 1000 c)
        deb     (s/debounce 2000 sampled) ]
    (s/consume println deb)))

(defn -main []
  (test-debounce))

(set! *main-cli-fn* -main)
