;;
;; Listing 14.2
;;
(ns joy.event-sourcing
  (:require [joy.generators :refer [rand-map]]))

(defn valid? [event]
  (boolean (:result event)))

(comment
  (valid? {})
  ;;=> false
  
  (valid? {:result 42})
  ;;=> true
  )


;;
;; Listing 14.3
;;
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
  ;;=> {:h 1, :avg 1.0, :ab 1}
  
  (effect {:ab 599 :h 180} {:result :out})
  ;;=> {:h 180, :avg 0.3, :ab 600}
  )


;;
;; Listing 14.4
;;
(defn apply-effect [state event]
  (if (valid? event)
    (effect state event)
    state))

(comment
  (apply-effect {:ab 600 :h 180 :avg 0.3}
                {:result :hit})
  ;;=> {:h 181, :avg 0.3011647254575707, :ab 601}
  )


;;
;; Listing 14.5
;;
(def effect-all #(reduce apply-effect %1 %2))

(comment
  (effect-all {:ab 0, :h 0}
    [{:result :hit}
     {:result :out}
     {:result :hit}
     {:result :out}])
  ;;=> {:h 2, :avg 0.5, :ab 4}
  )

(def events (repeatedly 100
              (fn []
                (rand-map 1
                  #(-> :result)
                  #(if (< (rand-int 10) 3)
                     :hit
                     :out)))))

(comment
  (effect-all {} events)
  ;;=> {:h 33, :avg 0.33, :ab 100}
  
  (effect-all {} (take 50 events))
  ;;=> {:h 14, :avg 0.28, :ab 50}
  )


(def fx-timeline #(reductions apply-effect %1 %2))
(comment
  (fx-timeline {} (take 3 events))
  ;;=> ({}
  ;;    {:ab 1, :h 0, :avg 0.0}
  ;;    {:ab 2, :h 0, :avg 0.0}
  ;;    {:ab 3, :h 1, :avg 0.3333333})
  )
