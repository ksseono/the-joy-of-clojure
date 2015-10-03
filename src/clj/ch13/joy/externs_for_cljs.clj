(ns joy.externs-for-cljs
  (:require [cljs.compiler :as comp]
            [cljs.analyzer :as ana]
            [clojure.walk :refer [prewalk]]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io])
  (:import (clojure.lang LineNumberingPushbackReader)))

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

(defn read-file
  "Read the contents of filename as a sequence of Clojure values."
  [filename]
  (let [eof (Object.)]
    (with-open [reader (LineNumberingPushbackReader.
                         (io/reader filename))]
      (doall
        (take-while #(not= % eof)
          (repeatedly #(read reader false eof)))))))

(defn file-ast
  "Return the ClojureScript AST for the contents of filename. Tends to be large
   and to contain cycles -- be careful printing at the REPL."
  [filename]
  (binding [ana/*cljs-ns* 'cljs.user
            ana/*cljs-file* filename]
    (mapv #(ana/analyze (ana/empty-env) %)
      (read-file filename))))

(comment
  (count (file-ast "src/cljs/joy/music.cljs"))
  (first (file-ast "src/cljs/joy/music.cljs")))

(defn flatten-ast [ast]
  (mapcat #(tree-seq :children :children %) ast))

(def flat-ast (flatten-ast (file-ast "src/cljs/joy/music.cljs")))

(comment (count flat-ast))

(defn get-interop-used
  "Return a set of symbols representing the method and field names
   used in interop forms in the given sequence of AST nodes."
  [flat-ast]
  (set (keep #(some % [:method :field]) flat-ast)))

(comment (get-interop-used flat-ast))

(defn externs-for-interop [syms]
  (apply str
    "var DummyClass={};\n"
    (map #(str "DummyClass." % "=function(){};\n")
      syms)))





