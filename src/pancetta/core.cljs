(ns pancetta.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.nodejs :as node]
            [cljs.core.async :refer [chan <! >! alts! timeout]]
            [pancetta.streams :as s]))

(node/enable-util-print!)

(defn test-debounce
  []
  (let [seq-chan (s/seq->chan 100 (range 100))
        deb      (s/debounce seq-chan 500)]
    (s/consume deb println)))

(defn test-seq
  []
  (let [seq-chan (s/seq->chan 10 (range 100))]
    (s/consume seq-chan println)))

(defn -main []
  (test-debounce))

(set! *main-cli-fn* -main)
