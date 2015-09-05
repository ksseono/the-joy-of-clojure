(ns joy.promises
  "section 11.2"
  (:require [joy.mutation :refer (dothreads!)])
  (:require [joy.futures :refer (feed-children)]))

(def x (promise))
(def y (promise))
(def z (promise))

(dothreads! #(deliver z (+ @x @y)))

(dothreads!
  #(do (Thread/sleep 2000) (deliver x 52)))

(dothreads!
  #(do (Thread/sleep 4000) (deliver y 86)))

(time @z)

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

(run-tests pass fail fail fail pass)


(defn feed-items [k feed]
  (k
    (for [item (filter (comp #{:entry :item} :tag)
                 (feed-children feed))]
      (-> item :content first :content))))

(feed-items
  count
  "http://blog.fogus.me/feed/")

(let [p (promise)]
  (feed-items #(deliver p (count %))
    "http://blog.fogus.me/feed/")
  @p)

(defn cps->fn [f k]
  (fn [& args]
    (let [p (promise)]
      (apply f (fn [x] (deliver p (k x))) args)
      @p)))

(def count-items (cps->fn feed-items count))

(count-items "http://blog.fogus.me/feed/")


(def kant (promise))
(def hume (promise))

(dothreads!
  #(do (println "Kant has" @kant) (deliver hume :thinking)))

(dothreads!
  #(do (println "Hume is" @hume) (deliver kant :fork)))











