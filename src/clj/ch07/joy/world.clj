(ns joy.world
  (:use [joy.neighbors]))

(def world [[  1   1   1   1   1]
            [999 999 999 999   1]
            [  1   1   1   1   1]
            [  1 999 999 999 999]
            [  1   1   1   1   1]])

;;
;; Listing 7.3
;;
(defn estimate-cost [step-cost-est size y x]
  (* step-cost-est
     (- (+ size size) y x 2)))

(comment
  (estimate-cost 900 5 0 0)
  ;;=> 7200

  (estimate-cost 900 5 4 4)
  ;;=> 0
  )


;;
;; Listing 7.4
;;
(defn path-cost [node-cost cheapest-nbr]
  (+ node-cost
     (or (:cost cheapest-nbr) 0)))

(comment
  (path-cost 900 {:cost 1})
  ;;=> 901
  )


;;
;; Listing 7.5
;;
(defn total-cost [newcost step-cost-est size y x]
  (+ newcost
     (estimate-cost step-cost-est size y x)))

(comment
  (total-cost 0 900 5 0 0)
  ;;=> 7200

  (total-cost 1000 900 5 3 4)
  ;;=> 1900

  (total-cost (path-cost 900 {:cost 1}) 900 5 3 4)
  ;;=> 1801
  )


;;
;; Listing 7.6
;;
(defn min-by [f coll]
  (when (seq coll)
    (reduce (fn [min other]
              (if (> (f min) (f other))
                other
                min))
            coll)))

(comment
  (min-by :cost [{:cost 100} {:cost 36} {:cost 9}])
  ;;=> {:cost 9}
  )


;;
;; Listing 7.7
;;
(defn astar [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (replicate size (vec (replicate size nil))))
           work-todo (sorted-set [0 start-yx])]
      (if (empty? work-todo)
        [(peek (peek routes)) :steps steps]
        (let [[_ yx :as work-item] (first work-todo)
              rest-work-todo (disj work-todo work-item)
              nbr-yxs (neighbors size yx)
              cheapest-nbr (min-by :cost
                                   (keep #(get-in routes %)
                                         nbr-yxs))
              newcost (path-cost (get-in cell-costs yx)
                                 cheapest-nbr)
              oldcost (:cost (get-in routes yx))]
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) routes rest-work-todo)
            (recur (inc steps)
                   (assoc-in routes yx
                             {:cost newcost
                              :yxs (conj (:yxs cheapest-nbr [])
                                         yx)})
                   (into rest-work-todo
                         (map
                          (fn [w]
                            (let [[y x] w]
                              [(total-cost newcost step-est size y x) w]))
                          nbr-yxs)))))))))


;;
;; Listing 7.8
;;
(comment
  (astar [0 0]
        900
        world)
;;=> [{:cost 17,
;;     :yxs [[0 0] [0 1] [0 2] [0 3] [0 4] [1 4] [2 4]
;;           [2 3] [2 2] [2 1] [2 0] [3 0] [4 0] [4 1]
;;           [4 2] [4 3] [4 4]]}
;;      :steps 94]
  )


;;
;; Listing 7.9
;;
(comment
  (astar [0 0]
         900
         [[   1   1   1   2   1]
          [   1   1   1 999   1]
          [   1   1   1 999   1]
          [   1   1   1 999   1]
          [   1   1   1   1   1]])

  ;; [{:cost 9,
  ;;   :yxs [[0 0] [0 1] [0 2]
  ;;         [1 2]
  ;;         [2 2]
  ;;         [3 2]
  ;;         [4 2] [4 3] [4 4]]}
  ;;   :steps 134]
  )

;;
;; Listing 7.10
;;
(comment
  (astar [0 0]
         900
         [[   1   1   1   2   1]
          [   1   1   1 999   1]
          [   1   1   1 999   1]
          [   1   1   1 999   1]
          [   1   1   1 666   1]])
  ;;=> [{:cost 10,
  ;;     :yxs [[0 0] [0 1] [0 2] [0 3] [0 4]
  ;;           [1 4]
  ;;           [2 4]
  ;;           [3 4]
  ;;           [4 4]]}
  ;;     :steps 132]
  )
