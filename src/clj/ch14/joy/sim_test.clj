;;
;; Listing 14.6
;;
(ns joy.sim-test
  (:require [joy.event-sourcing :as es]
            [joy.generators :refer (rand-map)]
            [clojure.set :as sql]))


(def PLAYERS #{{:player "Nick", :ability 32/100}
               {:player "Matt", :ability 26/100}
               {:player "Ryan", :ability 19/100}})

(defn lookup [db name]
  (first (sql/select
           #(= name (:player %))
           db)))

;;
;; Listing 14.7
;;
(comment
  (lookup PLAYERS "Nick")
  ;;=> {:ability 8/25, :player "Nick"}
  )


;;
;; Listing 14.8
;;
(defn update-stats [db event]
  (let [player (lookup db (:player event))
        less-db (sql/difference db #{player})]
    (conj less-db
      (merge player (es/effect player event)))))

(comment
  (update-stats PLAYERS {:player "Nick", :result :hit})
  ;;=> #{{:ability 13/50, :player "Matt"}
  ;;     {:ability 8/25, :player "Nick", :h 1, :avg 1.0, :ab 1}
  ;;     {:ability 19/100, :player "Ryan"}}
  )


;;
;; Listing 14.9
;;
(defn commit-event [db event]
  (dosync (alter db update-stats event)))

(comment
  (commit-event (ref PLAYERS) {:player "Nick", :result :hit})
  ;;=> #{{:ability 13/50, :player "Matt"}
  ;;     {:ability 8/25, :player "Nick", :h 1, :avg 1.0, :ab 1}
  ;;     {:ability 19/100, :player "Ryan"}}
  )


;;
;; Listing 14.10
;;
(defn rand-event [{ability :ability}]
  (let [able (numerator ability)
        max (denominator ability)]
    (rand-map 1
      #(-> :result)
      #(if (< (rand-int max) able)
           :hit
           :out))))


;;
;; Listing 14.11
;;
(defn rand-events [total player]
  (take total
    (repeatedly #(assoc (rand-event player)
                   :player
                   (:player player)))))

(comment
  (rand-events 3 {:player "Nick", :ability 32/100})
  ;;=> ({:result :hit, :player "Nick"}
  ;;    {:result :hit, :player "Nick"}
  ;;    {:result :out, :player "Nick"}
  )


;;
;; Listing 14.12
;;
(def agent-for-player
  (memoize
    (fn [player-name]
      (let [a (agent [])]
        (set-error-handler! a #(println "ERROR: " %1 %2))
        (set-error-mode! a :fail)
        a))))


;;
;; Listing 14.13
;;
(defn feed [db event]
  (let [a (agent-for-player (:player event))]
    (send a
      (fn [state]
        (commit-event db event)
        (conj state event)))))


;;
;; Listing 14.14
;;
(defn feed-all [db events]
  (doseq [event events]
    (feed db event))
  db)

(comment
  (let [db (ref PLAYERS)]
    (feed-all db (rand-events 100 {:player "Nick", :ability 32/100}))
    db)
  ;;=> #<Ref@321881a2: #{{:ability 19/100, :player "Ryan"}
  ;;                     {:ability 13/50,  :player "Matt"}
  ;;                     {:player "Nick", :h 27, :avg 0.27, :ab 100}}

  (count @(agent-for-player "Nick"))
  ;;=> 100
  
  (es/effect-all {} @(agent-for-player "Nick"))
  ;;=> {:ab 100, :h 27, :avg 0.27}
  )


;;
;; Listing 14.15
;;
(defn simulate [total players]
  (let [events (apply interleave
                (for [player players]
                  (rand-events total player)))
        results (feed-all (ref players) events)]
    (apply await (map #(agent-for-player (:player %)) players))
    @results))
    
(comment
  (simulate 2 PLAYERS)
  ;; #{{:ability 13/50, :player "Matt", :h 1, :avg 0.5, :ab 2}
  ;;   {:ability 8/25, :player "Nick", :h 1, :avg 0.5, :ab 2}
  ;;   {:ability 19/100, :player "Ryan", :h 0, :avg 0.0, :ab 2}}

  (simulate 400 PLAYERS)
  ;; #{{:ability 19/100, :player "Ryan", :h 77, :avg 0.1925, :ab 400}
  ;;   {:ability 13/50, :player "Matt", :h 110, :avg 0.275, :ab 400}
  ;;   {:ability 8/25, :player "Nick", :h 135, :avg 0.3375, :ab 400}}

  (es/effect-all {} @(agent-for-player "Nick"))
  ;;=> {:ab 402, :h 140, :avg 0.3482587064676617}
  )
