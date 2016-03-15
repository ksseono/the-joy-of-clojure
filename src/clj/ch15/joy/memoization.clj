(ns joy.memoization)

;;
;; Listing 15.1
;;
(defprotocol CacheProtocol
  (lookup [cache e])
  (has? [cache e])
  (hit [cache e])
  (miss [cache e ret]))


;;
;; Listing 15.2
;;
(deftype BasicCache [cache]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (has? [_ item]
    (contains? cache item))
  (hit [this itme] this)
  (miss [_ item result]
    (BasicCache. (assoc cache item result))))

(comment
  (def cache (BasicCache. {}))
  (lookup (miss cache '(servo) :robot) '(servo))
  ;;=> :robot
  )

(defn through [cache f item]
  (if (has? cache item)
    (hit cache item)
    (miss cache item (delay (apply f item)))))


;;
;; Listing 15.3
;;
(deftype PluggableMemoization [f cache]
  CacheProtocol
  (has? [_ item] (has? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (PluggableMemoization. f (miss cache item result)))
  (lookup [_ item]
    (lookup cache item)))


;;
;; Listing 15.4
;;
(defn memoization-impl [cache-impl]
  (let [cache (atom cache-impl)]
    (with-meta
      (fn [$ args]
        (let [cs (swap! cache through (.f cache-impl) args)]
          @(lookup cs args)))
      {:cache cache})))

(comment
  (def slowly (fn [x] (Thread/sleep 3000) x))
  (def sometimes-slowly (memoization-impl
                         (PluggableMemoization.
                          slowly
                          (BasicCache. {}))))

  (time [(sometimes-slowly 108) (sometimes-slowly 108)])
  ;; "Elapsed time: 3001.611 msecs"
  ;;=> [108 108]
  
  (time [(sometimes-slowly 108) (sometimes-slowly 108)])
  ;; "Elapsed time: 0.049 msecs"
  ;;=> [108 108]  
  )
