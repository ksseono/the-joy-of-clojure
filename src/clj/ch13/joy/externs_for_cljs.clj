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

;;
;; Listing 13.3
;;
(defn print-ast [ast]
  (pprint
    (prewalk
      (fn [x]
        (if (map? x)
          (select-keys x [:op :form :name :children])
          x))
      ast)))

(comment
  (print-ast ast)
  ;; {:op :def,
  ;;  :form
  ;;  (def hello (cljs.core/fn ([x] (js/alert (pr-str 'greetings x))))),
  ;;  :name cljs.user/hello,
  ;;  :children
  ;;  [{:op :fn,
  ;;    :form (fn* ([x] (js/alert (pr-str 'greetings x)))),
  ;;    :name {:name hello},
  ;;    :children
  ;;    [{:op :do,
  ;;      :form (do (js/alert (pr-str 'greetings x))),
  ;;      :children
  ;;      [{:op :invoke,
  ;;        :form (js/alert (pr-str 'greetings x)),
  ;;        :children
  ;;        [{:op :var, :form js/alert}
  ;;         {:op :invoke,
  ;;          :form (pr-str 'greetings x),
  ;;          :children
  ;;          [{:op :var, :form pr-str}
  ;;           {:op :constant, :form greetings}
  ;;           {:op :var, :form x}]}]}]}]}]}

  (comp/emit ast)
  ;; cljs.user.hello = (function cljs$user$hello(x){
  ;; return alert(cljs.user.pr_str.call(null,
  ;;   new cljs.core.Symbol(null,"greetings","greetings",
  ;;                        -547008995,null),x));
  ;; });
  ;;=> nil
  )

;;
;; Listing 13.8
;;
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
  ;;=> 13
  
  (first (file-ast "src/cljs/joy/music.cljs"))
  ;;=> {:use-macros nil, :excludes #{}, :name joy.music, ...}
  )

(defn flatten-ast [ast]
  (mapcat #(tree-seq :children :children %) ast))

(def flat-ast (flatten-ast (file-ast "src/cljs/joy/music.cljs")))

(comment
  (count flat-ast)
  ;;=> 557
  )

(defn get-interop-used
  "Return a set of symbols representing the method and field names
   used in interop forms in the given sequence of AST nodes."
  [flat-ast]
  (set (keep #(some % [:method :field]) flat-ast)))

(comment
  (get-interop-used flat-ast)
  ;;=> #{destination createDynamicsCompressor createOscillator createGain
  ;;     linearRampToValueAtTime connect value frequency start
  ;;     cljs$core$ISeq$ AudioContext currentTime stop
  ;;     cljs$lang$protocol_mask$partition0$ detune gain webkitAudioContext}
  )

(defn externs-for-interop [syms]
  (apply str
    "var DummyClass={};\n"
    (map #(str "DummyClass." % "=function(){};\n")
      syms)))
