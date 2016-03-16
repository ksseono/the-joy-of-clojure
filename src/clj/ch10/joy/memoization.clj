(ns joy.memoization
  "section 10.4")

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
(time [(slowly 9) (slowly 9)])
;; "Elapsed time: 2007.93021 msecs"
;; => [9 9]

(def sometimes-slowly (manipulable-memoize slowly))
(time [(sometimes-slowly 108) (sometimes-slowly 108)])
;; "Elapsed time: 1004.743564 msecs"
;; => [108 108]

(meta sometimes-slowly)
;;=> {:cache #object[clojure.lang.Atom 0x500809fa {:status :ready, :val {(108) 108}}]}

(let [cache (:cache (meta sometimes-slowly))]
  (swap! cache dissoc '(108)))
;;=> {}

(meta sometimes-slowly)
;;=> {:cache #object[clojure.lang.Atom 0x500809fa {:status :ready, :val {}}]}
(time [(sometimes-slowly 108) (sometimes-slowly 108)])
;; "Elapsed time: 1004.050977 msecs"
;; => [108 108]