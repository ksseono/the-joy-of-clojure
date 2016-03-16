(ns joy.debugging
  (:require [clojure.xml :as xml]))

(defn traverse [node f]
  (when node
    (f node)
    (doseq [child (:content node)]
      (traverse child f))))

(comment
  (traverse {:tag :flower :attrs {:name "Tanpopo"} :content []}
    println)
  ;; {:tag :flower, :attrs {:name Tanpopo}, :content []}
  )

(def DB
  (-> "<zoo>
        <pongo>
          <animal>orangutan</animal>
        </pongo>
        <panthera>
          <animal>Spot</animal>
          <animal>lion</animal>
          <animal>Lopshire</animal>
        </panthera>
      </zoo>"
    .getBytes
    (java.io.ByteArrayInputStream.)
    xml/parse))

;;
;; Listing 17.21
;;
(defn ^:dynamic handle-weird-animal
  [{[name] :content}]
  (throw (Exception. (str name " must be 'dealt with'"))))


;;
;; Listing 17.22
;;
(defmulti visit :tag)

(defmethod visit :animal [{[name] :content :as animal}]
  (case name
    "Spot" (handle-weird-animal animal)
    "Lopshire" (handle-weird-animal animal)
    (println name)))

(defmethod visit :default [node] nil)

(comment
  (traverse DB visit)
  ; orangutan
  ; Exception Spot must be 'dealt with'
  )

(defmulti handle-weird (fn [{[name] :content}] name))

(defmethod handle-weird "Spot" [_]
  (println "Transporting Spot to the circus."))

(defmethod handle-weird "Lopshire" [_]
  (println "Signing Lopshire to a book deal."))

(comment
  (binding [handle-weird-animal handle-weird]
    (traverse DB visit))
  ;; orangutan
  ;; Transporting Spot to the circus.
  ;; lion
  ;; Signing Lopshire to a book deal.

  (def _ (future
           (binding [handle-weird-animal #(println (:content %))]
             (traverse DB visit))))
  ;; orangutan
  ;; [Spot]
  ;; lion
  ;; [Lopshire]
  )


;;
;; Listing 17.23
;;
(defn readr [prompt exit-code]
  (let [input (clojure.main/repl-read prompt exit-code)]
    (if (= input ::tl)
      exit-code
      input)))

(comment
  (readr #(print "invisible=> ") ::exit)
  [1 2 3] ;; this is what you type
  ;;=> [1 2 3]

  (readr #(print "invisible=> ") ::exit)
  ;; ::tl ;; this is what you type
  ;;=> :joy.debugging/exit
  )


;;
;; Listing 17.24
;;
(defmacro local-context []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym))
              symbols)
      symbols)))

(comment
  (local-context)
  ;;=> {}

  (let [a 1, b 2, c 3]
    (let [b 200]
      (local-context)))
  ;;=> {a 1, b 200, c 3}
  )


;;
;; Listing 17.25
;;
(require '[joy.macros :refer (contextual-eval)])
(defmacro break []
  `(clojure.main/repl
     :prompt #(print "debug=> ")
     :read readr
     :eval (partial contextual-eval (local-context))))

(comment
  (defn div [n d] (break) (int (/ n d)))
  (div 10 0)

  debug=> n
  ;;=> 10

  debug=> d
  ;;=> 0

  debug=> (local-context)
  ;;=> {n 10, d 0}

  debug=> ::tl
  ;; ArithmeticException Divide by zero
  )


;;
;; Listing 17.26
;;
(defn keys-apply [f ks m]
  (break)
  (let [only (select-keys m ks)]
    (break)
    (zipmap (keys only) (map f (vals only)))))

(comment
  (keys-apply inc [:a :b] {:a 1, :b 2, :c 3})

  debug=> only
  ;; java.lang.RuntimeException: Unable to resolve symbol: only in this context

  debug=> ks
  ;;=> [:a :b]

  debug=> m
  ;;=> {:a 1, :b 2, :c 3}

  debug=> ::tl
  debug=> only
  ;;=> {a 1, :b 2}

  debug=> ::tl
  ;;=> {:a 2, :b 3}
  )


;;
;; Listing 17.27
;;
(defmacro awhen [expr & body]
  (break)
  `(let [~'it ~expr]
     (if ~'it
       (do (break) ~@body))))

(comment
  (awhen [1 2 3] (it 2))

  debug=> it
  ;; java.lang.RuntimeException: Unable to resolve symbol: it in this context
  
  debug=> expr
  ;;=> [1 2 3]

  debug=> body
  ;;=> ((it 2))

  debug=> ::tl
  debug=> it
  ;;=> [1 2 3]

  debug=> (it 1)
  ;;=>  2

  debug=> ::tl
  ;;=> 3
  )
