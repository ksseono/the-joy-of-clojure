(ns joy.persistent)

;;
;; Listing 5.2 - first viersion of pos function
;;
;; (defn pos [e coll]
;;   (let [cmp (if (map? coll)
;;               #(= (second %1) %2)
;;               #(= %1 %2))]
;;     (loop [s coll idx 0]
;;       (when (seq s)
;;         (if (cmp (first s) e)
;;           (if (map? coll)
;;             (first (first s))
;;             idx)
;;           (recur (next s) (inc id)))))))
;;
;; (comment
;;   (pos 3 [:a 1 :b 2 :c 3 :d 4])
;;   ;;=> 5
;;
;;   (pos :foo [:a 1 :b 2 :c 3 :d 4])
;;   ;;=> nil
;;
;;   (pos 3 {:a 1 :b 2 :c 3 :d 4})
;;   ;;=> :c
;;
;;   (pos \3 ":a 1 :b 2 :c 3 :d 4")
;;   ;;=> 13
;;   )

;;
;; second version of pos function
;;
;; (defn pos [e coll]
;;   (for [[i v] (index coll) :when (= e v)] i))
;;
;; (comment
;;   (pos 3 [:a 1 :b 2 :c 3 :d 4])
;;   ;;=> (5)
;;
;;   (pos 3 {:a 1, :b 2, :c 3, :d 4})
;;   ;;=> (:c)
;;
;;   (pos 3 [:a 3 :b 3 :c 3 :d 4])
;;   ;;=> (1 3 5)
;;
;;   (pos 3 {:a 3, :b 3, :c 3, :d 4})
;;   ;;=> (:a :c :b)
;;   )

(defn index [coll]
  (cond
   (map? coll) (seq coll)
   (set? coll) (map vector coll coll)
   :else (map vector (iterate inc 0) coll)))

(comment
  (index [:a 1 :b 2 :c 3 :d 4])
  ;;=> ([0 :a] [1 1] [2 :b] [3 2] [4 :c] [5 3] [6 :d] [7 4])

  (index {:a 1 :b 2 :c 3 :d 4})
  ;;=> ([:a 1] [:b 2] [:c 3] [:d 4])

  (index #{:a 1 :b 2 :c 3 :d 4})
  ;;=> ([1 1] [4 4] [:c :c] [3 3] [2 2] [:b :b] [:d :d] [:a :a])
  )

;;
;; the last version of pos function
;;
(defn pos [pred coll]
  (for [[i v] (index coll) :when (pred v)] i))

(comment
  (pos #{3 4} {:a 1 :b 2 :c 3 :d 4})
  ;;=> (:c :d)

  (pos even? [2 3 6 7])
  ;;=> (0 2)
  )