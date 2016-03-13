(ns joy.unit)

(defn convert [context descriptor]
  (reduce (fn [result [mag unit]]
            (+ result
              (let [val (get context unit)]
                (if (vector? val)
                  (* mag (convert context val))
                  (* mag val)))))
    0
    (partition 2 descriptor)))

;;
;; Listing 14.1
;;
(def distance-reader
  (partial convert
    {:m 1
     :km 1000,
     :cm 1/100,
     :mm [1/10 :cm]}))

(def time-reader
  (partial convert
    {:sec 1
     :min 60,
     :hr [60 :min],
     :day [24 :hr]}))

(comment
  #unit/length [1 :km]
  ;;=> 1000

  (binding [*data-readers* {'unit/time #'joy.unit/time-reader}]
    (read-string "#unit/time [1 :min 30 :sec]"))
  ;;=> 90

  (binding [*default-data-reader-fn* #(-> {:tag %1 :payload %2})]
    (read-string "#nope [:doesnt-exist]"))
  ;;=> {:tag nope, :payload [:doesnt-exist]}
  )


(require '[clojure.edn :as edn])

(def T {'unit/time #'joy.unit/time-reader})

(comment
  (edn/read-string {:readers T} "#unit/time [1 :min 30 :sec]")
  ;;=> 90
  
  (edn/read-string {:readers T, :default vector} "#what/the :huh?")
  ;;=> [what/the :huh?]  
  )


;;
;; Listing 14.16
;;
(defn relative-units [context unit]
  (if-let [spec (get context unit)]
    (if (vector? spec)
      (convert context spec)
      spec)
    (throw (RuntimeException. (str "Undefined unit " unit)))))

(comment
  (relative-units {:m 1, :cm 1/100, :mm [1/10 :cm]} :m)
  ;;=> 1
  
  (relative-units {:m 1, :cm 1/100, :mm [1/10 :cm]} :mm)
  ;;=> 1/1000
  
  (relative-units {:m 1, :cm 1/100, :mm [1/10 :cm]} :ramsden-chain)
  ;; RuntimeException Undefined unit :ramsden-chain  joy.unit/relative-units (unit.clj:65)
  )


;;
;; Listing 14.17
;;
(defmacro defunits-of [name base-unit & conversions]
  (let [magnitude (gensym)
        unit (gensym)
        units-map (into `{~base-unit 1}
                    (map vec (partition 2 conversions)))]
    `(defmacro ~(symbol (str "unit-of-" name))
       [~magnitude ~unit]
       `(* ~~magnitude
          ~(case ~unit
             ~@(mapcat
                 (fn [[u# & r#]]
                   `[~u# ~(relative-units units-map u#)])
                 units-map))))))

(defunits-of distance :m
  :km 1000
  :cm 1/100
  :mm [1/10 :cm]
  :ft 0.3048
  :mile [5280 :ft])

(comment
  (unit-of-distance 1 :m)
  ;;=> 1
  
  (unit-of-distance 1 :mm)
  ;;=> 1/1000
  
  (unit-of-distance 1 :ft)
  ;;=> 0.3048
  
  (unit-of-distance 1 :mile)
  ;;=> 1609.344
  )
