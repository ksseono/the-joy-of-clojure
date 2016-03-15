(ns joy.reducibles)

;;
;; Listing 15.11
;;
(defn empty-range? [start end step]
  (or (and (pos? step) (>= start end))
    (and (neg? step) (<= start end))))

(defn lazy-range [i end step]
  (lazy-seq
    (if (empty-range? i end step)
      nil
      (cons i
        (lazy-range (+ i step)
          end
          step)))))

(comment
  (lazy-range 5 10 2)
  ;;=> (5 7 9)
  
  (lazy-range 6 0 -1)
  ;;=> (6 5 4 3 2 1)

  (reduce conj [] (lazy-range 6 0 -1))
  ;;=> [6 5 4 3 2 1]
  
  (reduce + 0 (lazy-range 6 0 -1))
  ;;=> 21
  )


;;
;; Listing 15.12
;;
(defn reducible-range [start end step]
  (fn [reducing-fn init]
    (loop [result init, i start]
      (if (empty-range? i end step)
        result
        (recur (reducing-fn result i)
          (+ i step))))))

(defn half [x]
  (/ x 2))

(defn sum-half [result input]
  (+ result (half input)))

(defn half-transformer [f1]
  (fn f1-half [result input]
    (f1 result (half input))))

(comment
  (reduce sum-half 0 (lazy-range 0 10 2))
  ;;=> 10

  ((reducible-range 0 10 2) sum-half 0)
  ;;=> 10

  ((reducible-range 0 10 2) (half-transformer +) 0)
  ;;=> 10

  ((reducible-range 0 10 2) (half-transformer conj) [])
  ;;=> [0 1 2 3 4]  
  )


;;
;; Listing 15.13
;;
(defn mapping [map-fn]
  (fn map-transformer [f1]
    (fn [result input]
      (f1 result (map-fn input)))))

(comment
  ((reducible-range 0 10 2) ((mapping half) +) 0)
  ;;=> 10
  
  ((reducible-range 0 10 2) ((mapping half) conj) [])
  ;;=> [0 1 2 3 4]
  
  ((reducible-range 0 10 2) ((mapping list) conj) [])
  ;;=> [(0) (2) (4) (6) (8)]
  )


;;
;; Listing 15.14
;;
(defn filtering [filter-pred]
  (fn [f1]
    (fn [result input]
      (if (filter-pred input)
        (f1 result input)
        result))))

(comment
  ((reducible-range 0 10 2) ((filtering #(not= % 2)) +) 0)
  ;;=> 18
  
  ((reducible-range 0 10 2) ((filtering #(not= % 2)) conj) [])
  ;;=> [0 4 6 8]
  
  ((reducible-range 0 10 2)
   ((filtering #(not= % 2))
    ((mapping half) conj))
   [])
  ;;=> [0 2 3 4]

  ((reducible-range 0 10 2)
   ((mapping half)
    ((filtering #(not= % 2)) conj))
   [])
  ;;=> [0 1 3 4]
  )


;;
;; Listing 15.15
;;
(defn mapcatting [map-fn]
  (fn [f1]
    (fn [result input]
      (let [reducible (map-fn input)]
        (reducible f1 result)))))

(defn and-plus-ten [x]
  (reducible-range x (+ 11 x) 10))

(comment
  ((and-plus-ten 5) conj [])
  ;;=> [5 15]
  
  ((reducible-range 0 10 2) ((mapcatting and-plus-ten) conj) [])
  ;;=> [0 10 2 12 4 14 6 16 8 18]
  )


;;
;; Listing 15.16
;;
(defn r-map [mapping-fn reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((mapping mapping-fn) reducing-fn) init)))

(defn r-filter [filter-pred reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((filtering filter-pred) reducing-fn) init)))

(def our-final-reducible
  (r-filter #(not= % 2)
    (r-map half
      (reducible-range 0 10 2))))

(comment
  (our-final-reducible conj [])
  ;;=> [0 1 3 4]
  )


;;
;; Measuring Performance
;;
(require '[criterium.core :as crit])
(comment
  (crit/bench
   (reduce + 0 (filter even? (map half (lazy-range 0 (* 10 1000 1000) 2)))))
  ;; Execution time mean : 1.593855 sec

 (crit/bench
  (reduce + 0 (filter even? (map half (range 0 (* 10 1000 1000) 2)))))
 ;; Execution time mean : 603.006967 ms

 (crit/bench
  ((r-filter even? (r-map half (reducible-range 0 (* 10 1000 1000) 2))) + 0)
  ;; Execution time mean : 385.042958 ms
  )
 )


;;
;; Listing 15.17
;;
(require '[clojure.core.reducers :as r])

(defn core-r-map [mapping-fn core-reducible]
  (r/reducer core-reducible (mapping mapping-fn)))

(defn core-r-filter [filter-pred core-reducible]
  (r/reducer core-reducible (filtering filter-pred)))

(comment
  (reduce conj []
    (core-r-filter #(not= % 2) (core-r-map half [0 2 4 6 8])))
  ;;=> [0 1 3 4]
  ) 


;;
;; Listing 15.18
;;
(defn reduce-range [reducing-fn init, start end step]
  (loop [result init, i start]
    (if (empty-range? i end step)
      result
      (recur (reducing-fn result i)
        (+ i step)))))

(require '[clojure.core.protocols :as protos])
(defn core-reducible-range [start end step]
  (reify protos/CollReduce
    (coll-reduce [this reducing-fn init]
      (reduce-range reducing-fn init, start end step))
    (coll-reduce [this reducing-fn]
      (if (empty-range? start end step)
        (reducing-fn)
        (reduce-range reducing-fn start, (+ start step) end step)))))

(comment
  (reduce conj []
    (core-r-filter #(not= % 2)
      (core-r-map half (core-reducible-range 0 10 2))))
  ;;=> [0 1 3 4]

  (reduce + (core-reducible-range 10 12 1))
  ;;=> 21
  
  (reduce + (core-reducible-range 10 11 1))
  ;;=> 10
  
  (reduce + (core-reducible-range 10 10 1))
  ;;=> 0
  )


;;
;; Listing 15.19
;;
(defn core-f-map [mapping-fn core-reducible]
  (r/folder core-reducible (mapping mapping-fn)))

(defn core-f-filter [filter-pred core-reducible]
  (r/folder core-reducible (filtering filter-pred)))

(comment
  (r/fold +
    (core-f-filter #(not= % 2)
      (core-f-map half [0 2 4 6 8])))
  ;;=> 8

  (r/fold +
    (r/filter #(not= % 2)
       (r/map half [0 2 4 6 8])))
  ;;=> 8
  )

(comment
  ;; monoid
  (r/fold (r/monoid + (constantly 100)) (range 10))
  ;;=> 145

  (r/fold 512
          (r/monoid + (constantly 100))
          +
          (range 10))
  ;;=> 145

  (r/fold 4 (r/monoid conj (constantly [])) conj (vec (range 10)))
  ;;=> [0 1 [2 3 4] [5 6 [7 8 9]]]

  (r/fold 4 (r/monoid into (constantly [])) conj (vec (range 10)))
  ;;=> [0 1 2 3 4 5 6 7 8 9]

  (r/foldcat (r/filter even? (vec (range 1000))))
  ;; #object[clojure.core.reducers.Cat 0x13fb909a "clojure.core.reducers.Cat@13fb909a"]

  (seq (r/foldcat (r/filter even? (vec (range 10)))))
  ;;=> (0 2 4 6 8)

  (def big-vector (vec (range 0 (* 10 1000 1000) 2)))
  (crit/bench
   (r/fold + (core-f-filter even? (core-f-map half big-vector))))
  ;; Execution time mean : 126.756586 ms
  )
