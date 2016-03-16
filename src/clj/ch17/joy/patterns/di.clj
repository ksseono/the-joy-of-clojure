(ns joy.patterns.di
  (:require [joy.patterns.abstract-factory :as factory]))

(def lofi {:type :sim, :descr "Lowfi sim", :fidelity :low})
(def hifi {:type :sim, :descr "Hifi sim", :fidelity :high, :threads 2})

(comment
  (factory/construct :lofi lofi)
  ;;=> #joy.patterns.abstract_factory.LowFiSim{:name :lofi}
  )


;;
;; Listing 17.11
;;
(defprotocol Sys
  (start! [sys])
  (stop!  [sys]))

(defprotocol Sim
  (handle [sim msg]))


;;
;; Listing 17.12
;;
(defn build-system [name config]
  (let [sys (factory/construct name config)]
    (start! sys)
    sys))


;;
;; Listing 17.13
;;
(extend-type joy.patterns.abstract_factory.LowFiSim
  Sys
  (start! [this]
    (println "Started a lofi simulator."))
  (stop! [this]
    (println "Stopped a lofi simulator."))

  Sim
  (handle [this msg]
    (* (:weight msg) 3.14)))

(comment
  (start! (factory/construct :lofi lofi))
  ;; Started a lofi simulator.

  (build-system :sim1 lofi)
  ;; Started a lofi simulator.
  ;;=> #joy.patterns.abstract_factory.LowFiSim{:name :sim1}

  (handle (build-system :sim1 lofi) {:weight 42})
  ;;=> 131.88
  )


;;
;; Listing 17.14
;;
(extend-type joy.patterns.abstract_factory.HiFiSim
  Sys
  (start! [this] (println "Started a hifi simulator."))
  (stop! [this] (println "Stopped a hifi simulator."))

  Sim
  (handle [this msg]
    (Thread/sleep 5000)
    (* (:weight msg) 3.1415926535897932384626M)))

(comment
  (build-system :sim2 hifi)
  ;; Started a hifi simulator.
  ;;=> #joy.patterns.abstract_factory.HiFiSim{:name :sim2, :threads 2}

  (handle (build-system :sim2 hifi) {:weight 42})
  ;; Started a hifi simulator.
  ;; wait 5 seconds...
  ;;=> 131.9468914507713160154292M
  )


;;
;; Listing 17.15
;;
(def excellent (promise))

(defn simulate [answer fast slow opts]
  (future (deliver answer (handle slow opts)))
  (handle fast opts))

(comment
  (simulate excellent
    (build-system :sim1 lofi)
    (build-system :sim2 hifi)
    {:weight 42})
  ;;=> 131.88

  (realized? excellent)
  ;;=> false

  ;; wait a few seconds

  (realized? excellent)
  ;;=> true

  @excellent
  ;;=> 131.9468914507713160154292M
  )
