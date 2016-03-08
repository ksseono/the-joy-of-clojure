(ns joy.contracts)

;;
;; Listring 8.1
;;
(declare collect-bodies)

(defmacro contract [name & forms]
  (list* `fn name (collect-bodies forms)))

(declare build-contract)

(defn collect-bodies [forms]
  (for [form (partition 3 forms)]
    (build-contract form)))

;;
;; Listing 8.2
;;
(defn build-contract [c]
  (let [args (first c)]
    (list
      (into '[f] args)
      (apply merge
        (for [con (rest c)]
          (cond (= (first con) 'require)
                (assoc {} :pre (vec (rest con)))
                (= (first con) 'ensure)
                (assoc {} :post (vec (rest con)))
                :else (throw (Exception. (str "Unknown tag " (first con)))))))
      (list* 'f args))))

;;
;; Listing 8.3
;;
(def doubler-contract
  (contract doubler
    [x]
    (require (pos? x))
    (ensure (= (* 2 x) %))))

(def times2 (partial doubler-contract #(* 2 %)))
(def times3 (partial doubler-contract #(* 3 %)))

(comment
  (times2 9)
  ;;=> 18

  (times3 9)
  ;; AssertionError Assert failed: (= (* 2 x) %)
  )

;;
;; Listing 8.4
;;
(def doubler-contract
  (contract doubler
            [x]
            (require (pos? x))
            (ensure (= (* 2 x) %))
            [x y]
            (require (pos? x)
                     (pos? y))
            (ensure
             (= (* 2 (+ x y)) %))))

(comment
  ((partial doubler-contract #(* 2 (+ %1 %2))) 2 3)
  ;;=> 10

  ((partial doubler-contract #(+ %1 %1 %2 %2)) 2 3)
  ;;=> 10

  ((partial doubler-contract #(* 3 (+ %1 %2))) 2 3)
  ;; AssertionError Assert failed: (= (* 2 (+ x y)) %)
  )
