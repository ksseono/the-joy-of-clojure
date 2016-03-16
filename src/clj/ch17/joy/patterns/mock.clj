;;
;; Listing 17.16
;;
(ns joy.patterns.mock
  (:require [joy.patterns.abstract-factory :as factory]
            [joy.patterns.di :as di]))

(defrecord MockSim [name])

(def starts (atom 0))


;;
;; Listing 17.17
;;
(extend-type MockSim
  di/Sys
  (start! [this]
    (if (= 1 (swap! starts inc))
      (println "Started a mock simulator.")
      (throw (RuntimeException. "Called start! more than once."))))
  (stop! [this]
    (println "Stopped a mock simulator."))

  di/Sim
  (handle [_ _] 42))


;;
;; Listing 17.18
;;
(defmethod factory/construct [:mock nil]
  [nom _]
  (MockSim. nom))
