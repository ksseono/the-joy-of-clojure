(ns joy.qsort)

(defn rand-ints [n]
  (take n (repeatedly #(rand-int n))))

;;
;; Listing 6.4
;;
(defn sort-parts [work]
  (lazy-seq
   (loop [[part & parts] work]
     (if-let [[pivot & xs] (seq part)]
       (let [smaller? #(< % pivot)]
         (recur (list*
                 (filter smaller? xs)
                 pivot
                 (remove smaller? xs)
                 parts)))
       (when-let [[x & parts] parts]
         (cons x (sort-parts parts)))))))

(defn qsort [xs]
  (sort-parts (list xs)))

(comment
  (qsort [2 1 4 3])
  ;;=> (1 2 3 4)

  (qsort (rand-ints 20))
  ;;=> (0 0 1 1 3 5 6 9 9 11 12 13 15 15 16 19 19 19 19 19)

  (first (qsort (rand-ints 100)))
  ;;=> 1

  (take 10 (qsort (rand-ints 10000)))
  ;;=> (4 4 5 6 6 7 8 8 8 9)
  )
