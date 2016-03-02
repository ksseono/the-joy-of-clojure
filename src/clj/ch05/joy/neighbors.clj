(ns joy.neighbors)

(def a-to-j (vec (map char (range 65 75))))

(def matrix
  [[1 2 3]
   [4 5 6]
   [7 8 9]])

(comment
  (get-in matrix [1 2])
  ;;=> 6

  (assoc-in matrix [1 2] 'x)
  ;;=> [[1 2 3] [4 5 x] [7 8 9]]
  )

;;
;; Listing 5.1
;;
(defn neighbors
  ([size yx] (neighbors [[-1 0] [1 0] [0 -1] [0 1]]
                        size
                        yx))
  ([deltas size yx]
   (filter (fn [new-yx]
             (every? #(< -1 % size) new-yx))
           (map #(vec (map + yx %))
                deltas))))

(comment
  (neighbors 3 [0 0])
  ;;=> ([1 0] [0 1])

  (neighbors 3 [1 1])
  ;;=> ([0 1] [2 1] [1 0] [1 2])

  (map #(get-in matrix %) (neighbors 3 [0 0]))
  ;;=> (4 2)  
  )

(defn strict-map1 [f col1]
  (loop [col1 col1, acc nil]
    (if (empty? col1)
      (reverse acc)
      (recur (next col1)
             (cons (f (first col1)) acc)))))

(defn strict-map2 [f col1]
  (loop [col1 col1, acc []]
    (if (empty? col1)
      acc
      (recur (next col1)
             (conj acc (f (first col1)))))))

(comment
  (strict-map1 - (range 5))
  ;;=> (0 -1 -2 -3 -4)

  (strict-map2 - (range 5))
  ;;=> [0 -1 -2 -3 -4]

  (subvec a-to-j 3 6)
  ;;=> [\D \E \F]
  )
