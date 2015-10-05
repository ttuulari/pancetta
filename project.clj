(defproject pancetta "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]]
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-npm "0.6.1"]]
  :npm {:dependencies [[source-map-support "0.3.2"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["out/pancetta" "out/pancetta.js"]
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :main pancetta.core 
                :output-to "out/pancetta.js"
                :output-dir "out"
                :optimizations :none
                :target :nodejs
                :cache-analysis true
                :source-map "out/pancetta.js.map"}}]})
