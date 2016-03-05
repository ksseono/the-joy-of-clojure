(ns joy.elevator)

(defn elevator [commands]
  (letfn
      [(ff-open [[_ & r]]
         "When the elevator is open on the 1st floor it can either close or be done."
         #(case _
            :close (ff-closed r)
            :done true
            false))
       (ff-closed [[_ & r]]
         "When the elevator is closed on the 1st floor it can either open or go up."
         #(case _
            :open (ff-open r)
            :up (sf-closed r)
            false))
       (sf-closed [[_ & r]]
         "When the elevator is closed on the 2nd floor it can either go down or open."
         #(case _
            :down (ff-closed r)
            :open (sf-open r)
            false))
       (sf-open [[_ & r]]
         "When the elevator is open on the 2nd floor it can either close or be done."
         #(case _
            :close (sf-closed r)
            :done true
            false))]
    (trampoline ff-open commands)))

(comment
  (elevator [:close :open :close :up :open :open :done])
  ;;=> false

  (elevator [:close :up :open :close :down :open :done])
  ;;=> true

  (elevator (cycle [:close :open]))
  ;; ... runs forever
  )
