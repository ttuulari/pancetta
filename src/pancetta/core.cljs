(ns pancetta.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as node]
            [cljs.core.async :as async :refer [chan <! >! alts! timeout]]
            [pancetta.streams :as s]))

(defn test-sampling
  []
  (let [c       (chan)
        _       (async/onto-chan c (range 5))
        sampled (s/sample 100 c)]
    (s/consume println sampled)))

(defn -main []
  (test-sampling))

(set! *main-cli-fn* -main)
