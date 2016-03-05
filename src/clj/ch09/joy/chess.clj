(ns joy.chess)

(defrecord Move [from to castle? promotion]
  Object
  (toString [this]
    (str "Move " (:from this)
         " to " (:to this)
         (if (:castle? this) " castle"
             (if-let [p (:promotion this)]
               (str " promote to " p)
               "")))))

(comment
  (str (Move. "e2" "e4" nil nil))
  ;;=> "Move e2 to e4"

  (.println System/out (Move. "e7" "e8" nil \Q))
  ;; Move e7 to e8 promote to Q
  )

(defn build-move [& {:keys [from to castle? promotion]}]
  {:pre [from to]}
  (Move. from to castle? promotion))

(comment
  (str (build-move :from "e2" :to "e4"))
  ;;=> "Move e2 to e4"
  )
