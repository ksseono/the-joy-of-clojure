(ns joy.atoms
  "section 10.4"
  (:use [joy.mutation :only [dothreads!]]))

(def ^:dynamic *time* (atom 0))
(defn tick [] (swap! *time* inc))
(dothreads! tick :threads 1000 :times 100)
@*time*