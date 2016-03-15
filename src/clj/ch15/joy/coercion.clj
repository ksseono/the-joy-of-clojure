(ns joy.coercion)

;;
;; Listing 15.5
;;
(defn factorial-a [original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (factorial-a 10)
  ;;=> 3628800

  (factorial-a 20)
  ;;=> 2432902008176640000   
  
  (time (dotimes [_ 1e5] (factorial-a 20)))
  ;; "Elapsed time: 172.914384 msecs"
  )


;;
;; Listing 15.6
;;
(defn factorial-b [original-x]
  (loop [x (long original-x), acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (time (dotimes [_ 1e5] (factorial-b 20)))
  ;; "Elapsed time: 44.687297 msecs"
  )


;;
;; Listing 15.7
;;
(defn factorial-c [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (time (dotimes [_ 1e5] (factorial-c 20)))
  ;; "Elapsed time: 43.797143 msecs"
  )


;;
;; Listing 15.8
;;
(set! *unchecked-math* true)

(defn factorial-d [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(set! *unchecked-math* false)

(comment
  (time (dotimes [_ 1e5] (factorial-d 20)))
  ;; "Elapsed time: 15.674197 msecs"

  (factorial-d 21)
  ;;=> -4249290049419214848

  (factorial-a 21)
  ;; ArithmeticException integer overflow
  )


;;
;; Listing 15.9
;;
(defn factorial-e [^double original-x]
  (loop [x original-x, acc 1.0]
    (if (>= 1.0 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (factorial-e 10.0)
  ;;=> 3628800.0
  
  (factorial-e 20.0)
  ;;=> 2.43290200817664E18
  
  (factorial-e 30.0)
  ;;=> 2.652528598121911E32
  
  (factorial-e 171.0)
  ;;=> Infinity
  
  (time (dotimes [_ 1e5] (factorial-e 20.0)))
  ;; "Elapsed time: 15.678149 msecs"
  )


;;
;; Listing 15.10
;;
(defn factorial-f [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (*' x acc)))))

(comment
  (factorial-f 20)
  ;;=> 2432902008176640000
  
  (factorial-f 30)
  ;;=> 265252859812191058636308480000000N
  
  (factorial-f 171)
  ;;=> 124101... this goes on a while ...0000N
  
  (time (dotimes [_ 1e5] (factorial-f 20)))
  ;; "Elapsed time: 101.7621 msecs"
  )
