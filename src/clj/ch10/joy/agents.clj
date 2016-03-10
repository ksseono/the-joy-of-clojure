(ns joy.agents
  (:use [joy.mutation :only [dothreads!]]))

(def log-agent (agent 0))

(defn do-log [msg-id message]
  (println msg-id ":" message)
  (inc msg-id))

;;
;; Listing 10.5
;;
(defn do-step [channel message]
  (Thread/sleep 1)
  (send-off log-agent do-log (str channel message)))

(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warming up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (dothreads! #(three-step "alpha"))
  (dothreads! #(three-step "beta"))
  (dothreads! #(three-step "omega")))

(comment
  (all-together-now)
  ;; 0 : beta ready to begin (step 0)
  ;; 1 : alpha ready to begin (step 0)
  ;; 2 : omega ready to begin (step 0)
  ;; 3 : beta warming up (step 1)
  ;; 4 : alpha warming up (step 1)
  ;; 5 : omega warming up (step 1)
  ;; 6 : beta really getting going now (step 2)
  ;; 7 : alpha really getting going now (step 2)
  ;; 8 : omega really getting going now (step 2)
  ;; 9 : alpha done! (step 3)
  ;; 10 : omega done! (step 3)
  ;; 11 : beta done! (step 3)

  @log-agent
  ;; 12
  )
