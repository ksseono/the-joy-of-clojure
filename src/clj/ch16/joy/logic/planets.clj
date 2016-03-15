(ns joy.logic.planets
  (:require [clojure.core.logic :as logic])
  (:require [clojure.core.logic.pldb :as pldb])
  (:require [clojure.core.logic.fd :as fd]))

(pldb/db-rel orbits orbital body)
(pldb/db-rel stars star)

(def facts
  (pldb/db
    [orbits :mercury :sun]    
    [orbits :venus :sun]    
    [orbits :earth :sun]    
    [orbits :mars :sun]    
    [orbits :jupiter :sun]
    [orbits :saturn :sun]
    [orbits :uranus :sun]
    [orbits :neptune :sun]
    [orbits :Bb :alpha-centauri]
    [orbits :moon :earth]
    [orbits :phobos :mars]
    [orbits :deimos :mars]
    [orbits :io :jupiter]
    [orbits :europa :jupiter]
    [orbits :ganymede :jupiter]
    [orbits :callisto :jupiter]

    [stars :sun]
    [stars :alpha-centauri]))


;;
;; Listing 16.8
;;
(pldb/with-db facts
  (logic/run* [q]
    (logic/fresh [orbital body]
      (orbits orbital body)
      (logic/== q orbital))))
;;=> (:saturn :earth :uranus :neptune :mars :jupiter :venus :mercury)


;;
;; planeto subgoal
;;
(defn planeto [body]
  (logic/fresh [star]
    (stars star)
    (orbits body star)))

(comment
  (pldb/with-db facts
    (logic/run* [q]
      (planeto :earth)))
  ;;=> (_0)

  (pldb/with-db facts
    (logic/run* [q]
      (planeto :earth)
      (logic/== q true)))
  ;;=> (true)

  (pldb/with-db facts
    (logic/run* [q]
      (planeto :sun)
      (logic/== q true)))
  ;;=> ()

  (pldb/with-db facts
    (logic/run* [q]
      (logic/fresh [orbital]
        (planeto orbital)
        (logic/== q orbital))))
  ;;=> (:Bb :saturn :earth :uranus :neptune :mars :jupiter :venus :mercury)

  (pldb/with-db facts
    (logic/run* [q]
      (planeto :Bb)))
  ;;=> (_0)
  )

;;
;; satelliteo subgoal
;;
(defn satelliteo [body]
  (logic/fresh [p]
    (orbits body p)
    (planeto p)))

(comment
  (pldb/with-db facts
    (logic/run* [q]
      (satelliteo :sun)))
  ;;=> ()

  (pldb/with-db facts
    (logic/run* [q]
      (satelliteo :earth)))
  ;;=> ()

  (pldb/with-db facts
    (logic/run* [q]
      (satelliteo :moon)))
  ;;=> (_0)

  (pldb/with-db facts
    (logic/run* [q]
      (satelliteo :io)))
  ;;=> (_0)

  (pldb/with-db facts
    (logic/run* [q]
      (orbits :leda :jupiter)))
  ;;=> ()
  )
