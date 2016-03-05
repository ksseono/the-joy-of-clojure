(ns joy.closures)

(defn times-n [n]
  (fn [y] (* y n)))

(def times-four (times-n 4))

(comment
  (times-four 10)
  ;;=> 40
  )

(defn filter-divisible [denom s]
  (filter #(zero? (rem % denom)) s))

(comment
  (filter-divisible 4 (range 10))
  ;;=> (0 4 8)

  (filter-divisible 5 (range 20))
  ;;=> (0 5 10 15)
  )

(def bearings [{:x  0, :y  1}    ; north
               {:x  1, :y  0}    ; east
               {:x  0, :y -1}    ; south
               {:x -1, :y  0}])  ; west

(defn forward [x y bearing-num]
  [(+ x (:x (bearings bearing-num)))
   (+ y (:y (bearings bearing-num)))])

(comment
  (forward 5 5 0)
  ;;=> [5 6]

  (forward 5 5 1)
  ;;=> [6 5]

  (forward 5 5 2)
  ;;=> [5 4]
  )

(defn bot [x y bearing-num]
  {:coords [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y (bearings bearing-num)))
                        bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left (fn [] (bot x y (mod (- 1 bearing-num) 4)))})

(comment
  (:coords (bot 5 5 0))
  ;;=> [5 5]

  (:bearing (bot 5 5 0))
  ;;=> :north

  (:coords ((:forward (bot 5 5 0))))
  ;;=> [5 6]

  (:bearing ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))
  ;;=> :east

  (:coords ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))
  ;;=> [7 5]
  )
