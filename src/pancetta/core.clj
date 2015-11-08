(ns pancetta.core
  (:require [clojure.core.async :as async :refer [go go-loop chan <! >! alts! timeout]]
            [pancetta.streams :as s]))

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


