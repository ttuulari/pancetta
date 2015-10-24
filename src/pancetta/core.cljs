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
    (-> c
         (s/sample 1000)
         (s/consume println))))

(defn test-debounce
  []
  (let [c       (async/chan)
        _       (async/onto-chan c (range 5))]
    (-> c
         (s/sample 1000)
         (s/debounce 2000)
         (s/consume println))))

(defn test-xf
  []
  (let [c       (async/chan)
        _       (async/onto-chan c (range 20))
        xf      (comp (filter even?)
                      (map #(* 10 %)))]
    (-> c
        (s/sample 500)
        (s/pipe-trans xf)
        (s/consume println))))

(defn -main []
  (test-xf))

(set! *main-cli-fn* -main)
