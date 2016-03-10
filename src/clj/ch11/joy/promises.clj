(ns joy.promises
  (:require [joy.mutation :refer (dothreads!)])
  (:require [joy.futures :refer (feed-children)]))

(def x (promise))
(def y (promise))
(def z (promise))

(comment
  (dothreads! #(deliver z (+ @x @y)))

  (dothreads!
   #(do (Thread/sleep 2000) (deliver x 52)))

  (dothreads!
   #(do (Thread/sleep 4000) (deliver y 86)))

  (time @z)
  ;; "Elapsed time: 3115.154625 msecs"
  ;; 138
  )


;;
;; Listing 11.8
;;
(defmacro with-promises [[n tasks _ as] & body]
  (when as
    `(let [tasks# ~tasks
           n# (count tasks#)
           promises# (take n# (repeatedly promise))]
       (dotimes [i# n#]
         (dothreads!
           (fn []
             (deliver (nth promises# i#)
               ((nth tasks# i#))))))
       (let [~n tasks#
             ~as promises#]
         ~@body))))


;;
;; Listing 11.9
;;
(defrecord TestRun [run passed failed])

(defn pass [] true)
(defn fail [] false)

(defn run-tests [& all-tests]
  (with-promises
    [tests all-tests :as results]
    (into (TestRun. 0 0 0)
      (reduce #(merge-with + %1 %2) {}
        (for [r results]
          (if @r
            {:run 1 :passed 1}
            {:run 1 :failed 1}))))))

(comment
  (run-tests pass fail fail fail pass)
  ;;=> #joy.promises.TestRun{:run 5, :passed 2, :failed 3}
  )


(defn feed-items [k feed]
  (k
    (for [item (filter (comp #{:entry :item} :tag)
                 (feed-children feed))]
      (-> item :content first :content))))

(comment
  (feed-items
   count
   "http://blog.fogus.me/feed/")
  ;;=> 5

  (let [p (promise)]
    (feed-items #(deliver p (count %))
       "http://blog.fogus.me/feed/")
    @p)
  ;;;=> 5
  )


;;
;; Listing 11.10
;;
(defn cps->fn [f k]
  (fn [& args]
    (let [p (promise)]
      (apply f (fn [x] (deliver p (k x))) args)
      @p)))

(def count-items (cps->fn feed-items count))

(comment
  (count-items "http://blog.fogus.me/feed/")
  ;;=> 5
  )
