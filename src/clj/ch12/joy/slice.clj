;;
;; Listing 12.15
;;
(ns joy.slice)

(definterface ISliceable
  (slice [^long s ^long e])
  (^int sliceCount []))


;;
;; Listing 12.16
;;
(def dumb
  (reify ISliceable
    (slice [_ s e] [:empty])
    (sliceCount [_] 42)))

(comment
  (.slice dumb 1 2)
  ;;=> [:empty]
  
  (.sliceCount dumb)
  ;;=> 42
  )


;;
;; Listing 12.17
;;
(defprotocol Sliceable
  (slice [this s e])
  (sliceCount [this]))

(extend ISliceable
  Sliceable
  {:slice (fn [this s e] (.slice this s e))
   :sliceCount (fn [this] (.sliceCount this))})

(comment
  (sliceCount dumb)
  ;;=> 42
  
  (slice dumb 0 0)
  ;;=> [:empty]
  )


;;
;; Listing 12.18
;;
(defn calc-slice-count [thing]
  "Calculates the number of possible slices using the formula:
      (n + r - 1)!
      ------------
      r!(n - 1)!
   where n is (count thing) and r is 2"
  (let [! #(reduce * (take % (iterate inc 1)))
        n (count thing)]
    (/ (! (- (+ n 2) 1))
       (* (! 2) (! (- n 1))))))

(extend-type String
  Sliceable
  (slice [this s e] (.substring this s (inc e)))
  (sliceCount [this] (calc-slice-count this)))

(comment
  (slice "abc" 0 1)
  ;;=> "ab"

  (sliceCount "abc")
  ;;=> 6
  )
