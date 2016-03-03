(ns joy.laziness)

;;
;; Listing 6.1
;;
(defn if-chain [x y z]
  (if x
    (if y
      (if z
        (do
          (println "Made it!")
          :all-truthy)))))

(defn and-chain [x y z]
  (and x y z (do (println "Made it!") :all-truthy)))

(comment
  (if-chain () 42 true)
  ;; Made it!
  ;;=> :all-truthy

  (if-chain true true false)
  ;;=> nil

  (and-chain () 42 true)
  ;; Made it!
  ;;=> :all-truthy

  (and-chain true false true)
  ;;=> false
  )

;;
;; Listing 6.2
;;
(defn lz-rec-step [s]
  (lazy-seq
   (if (seq s)
     [(first s) (lz-rec-step (rest s))]
     [])))

(comment
  (lz-rec-step [1 2 3 4])
  ;;=> (1 (2 (3 (4 ()))))

  (class (lz-rec-step [1 2 3 4]))
  ;;=> clojure.lang.LazySeq

  (dorun (lz-rec-step (range 200000)))
  ;;=> nil
  )


;;
;; Listing 6.3
;;
(defn triangle [n]
  (/ (* n (+ n 1)) 2))

(comment
  (triangle 10)
  ;;=> 55
  )

(def tri-nums (map triangle (iterate inc 1)))

(comment
  (take 10 tri-nums)
  ;;=> (1 3 6 10 15 21 28 36 45 55)

  (take 10 (filter even? tri-nums))
  ;;=> (6 10 28 36 66 78 120 136 190 210)

  (nth tri-nums 99)
  ;;=> 5050

  (double (reduce + (take 1000 (map / tri-nums))))
  ;;=> 1.998001998001998

  (take 2 (drop-while #(< % 10000) tri-nums))
  ;;=> (10011 10153)
  )
