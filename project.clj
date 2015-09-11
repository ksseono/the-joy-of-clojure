(defproject joyofclojure-2nd-shkim "0.1"
  :description "The Joy of Clojure 2nd"
  :url "http://www.joyfoclojure.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.unify "0.5.6"]
                 [org.clojure/core.logic "0.8.10"]
                 [com.cemerick/piggieback "0.2.1"]]
  :source-paths ["src/clj/ch10" "src/clj/ch11" "src/clj/ch12" "src/clj/ch13"]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src/cljs"]
     :compiler
     {:output-to "dev-target/all.js"
      :optimizations :whitespace
      :pretty-print true}}
    {:source-paths ["src/cljs"]
     :compiler
     {:output-to "prod-target/all.js"
      :optimizations :advanced
      :externs ["externs.js"]
      :pretty-print false}}]}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})















