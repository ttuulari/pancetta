(ns pancetta.core
  (:require [cljs.nodejs :as node]
            [cljs.core.async :refer [chan <! >! alts!]]
            [pancetta.streams :as streams]))

(node/enable-util-print!)

(defn -main []
  (streams/debounce (chan) 1000)
  (println "Hello world!"))

(set! *main-cli-fn* -main)
