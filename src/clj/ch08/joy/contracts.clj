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
(def doubler
  (contract c
    [x]
    (require (pos? x))
    (ensure (= (* 2 x) %))))

(def doubling
  (contract c [x]
    (require (number? x))
    (ensure (= % (* 2 x)))))

(def checked-doubler (partial doubling doubler))

(comment
  (checked-doubler "")
  ;; AssertionError Assert failed: (number? x)
  )

