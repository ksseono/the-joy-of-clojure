(ns joy.event-sourcing
  (:require [joy.generators :refer [rand-map]]))

(defn valid? [event]
  (boolean (:result event)))

(comment
  (valid? {}) ;;=> false
  (valid? {:result 42}) ;;=> true
  )

(defn effect [{:keys [ab h] :or {ab 0, h 0}}
              event]
  (let [ab (inc ab)
        h (if (= :hit (:result event))
            (inc h)
            h)
        avg (double (/ h ab))]
    {:ab ab :h h :avg avg}))

(comment
  (effect {} {:result :hit})
  (effect {:ab 599 :h 180} {:result :out}))

(defn apply-effect [state event]
  (if (valid? event)
    (effect state event)
    state))

(comment
  (apply-effect {:ab 600 :h 180 :avg 0.3}
    {:result :hit}))

(def effect-all #(reduce apply-effect %1 %2))

(comment
  (effect-all {:ab 0, :h 0}
    [{:result :hit}
     {:result :out}
     {:result :hit}
     {:result :out}]))

(def events (repeatedly 100
              (fn []
                (rand-map 1
                  #(-> :result)
                  #(if (< (rand-int 10) 3)
                     :hit
                     :out)))))

(comment
  (effect-all {} events)
  (effect-all {} (take 50 events)))


(def fx-timeline #(reductions apply-effect %1 %2))
(fx-timeline {} (take 3 events))
