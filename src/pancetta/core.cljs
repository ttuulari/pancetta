(ns pancetta.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as async :refer [chan <! >! alts! timeout]]
            [pancetta.streams :as s]))

(node/enable-util-print!)

(defn test-sampling
  []
  (let [c       (async/chan)
        _       (async/onto-chan c (range 5))]
    (->> c
         (s/sample 1000)
         (s/consume println))))

(defn test-debounce
  []
  (let [c       (async/chan)
        _       (async/onto-chan c (range 5))]
    (->> c
         (s/sample 1000)
         (s/debounce 2000)
         (s/consume println))))

(defn -main []
  (test-debounce))

(set! *main-cli-fn* -main)
