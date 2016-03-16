(ns joy.patterns.abstract-factory)

(def config
  '{:systems {:pump {:type :feeder, :descr "Feeder system"}
              :sim1 {:type :sim,    :fidelity :low}
              :sim2 {:type :sim,    :fidelity :high, :threads 2}}})

(defn describe-system [name cfg]
  [(:type cfg) (:fidelity cfg)])

(comment
  (describe-system :pump {:type :feeder, :descr "Feeder system"})
  ;;=> [:feeder nil]
  )


;;
;; Listing 17.9
;;
(defmulti construct describe-system)

(defmethod construct :default [name cfg]
  {:name name
   :type (:type cfg)})

(defn construct-subsystems [sys-map]
  (for [[name cfg] sys-map]
    (construct name cfg)))

(comment
  (construct-subsystems (:systems config))
  ;;=> ({:name :pump, :type :feeder}
  ;;    {:name :sim1, :type :sim}
  ;;    {:name :sim2, :type :sim})
  )

(defmethod construct [:feeder nil]
  [_ cfg]
  (:descr cfg))

(comment
  (construct-subsystems (:systems config))
  ;; ("Feeder system"
  ;;  {:name :sim1, :type :sim}
  ;;  {:name :sim2, :type :sim})
  )

(defrecord LowFiSim [name])
(defrecord HiFiSim [name threads])


;;
;; Listing 17.10
;;
(defmethod construct [:sim :low]
  [name cfg]
  (->LowFiSim name))

(defmethod construct [:sim :high]
  [name cfg]
  (->HiFiSim name (:threads cfg)))

(comment
  (construct-subsystems (:systems config))
  ;; ("Feeder system"
  ;;  {:name :sim1}
  ;;  {:name :sim2, :threads 2})
  )
