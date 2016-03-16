(ns joy.scalars)

;;
;; Listing 4.1
;;
(defn pour [lb ub]
  (cond
    (= ub :toujours) (iterate inc lb)
    :else (range lb ub)))

(comment
  (pour 1 10)
  ;;=> (1 2 3 4 5 6 7 8 9)

  (pour 1 :toujours)
  ;; ... runs forever
  )











