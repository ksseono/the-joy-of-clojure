(ns joy.atoms
  (:use [joy.mutation :only [dothreads!]]))

;;
;; Listing 10.6
;;
(defn manipulable-memoize [function]
  (let [cache (atom {})]
    (with-meta
      (fn [& args]
        (or (second (find @cache args))
            (let [ret (apply function args)]
              (swap! cache assoc args ret)
              ret)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1000) x))
(def sometimes-slowly (manipulable-memoize slowly))

(comment
  (time [(slowly 9) (slowly 9)])
  ;; "Elapsed time: 2007.40908 msecs"
  ;;=> [9 9]

  (time [(sometimes-slowly 108) (sometimes-slowly 108)])
  ;; "Elapsed time: 1007.108576 msecs"
  ;; [108 108]
  )

