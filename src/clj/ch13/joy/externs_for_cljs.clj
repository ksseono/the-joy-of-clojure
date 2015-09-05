(ns joy.externs-for-cljs
  (:require [cljs.compiler :as comp]
            [cljs.analyzer :as ana]
            [clojure.walk :refer [prewalk]]
            [clojure.pprint :refer [pprint]]))

(def code-string "(defn hello [x] (js/alert (pr-str 'greetings x)))")
(def code-data (read-string code-string))
(def ast (ana/analyze (ana/empty-env) code-data))

(defn print-ast [ast]
  (pprint
    (prewalk
      (fn [x]
        (if (map? x)
          (select-keys x [:op :form :name :children])
          x))
      ast)))


