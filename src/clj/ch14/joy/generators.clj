(ns joy.generators)

(def ascii (map char (range 65 (+ 65 26))))

(defn rand-str [sz alphabet]
  (apply str (repeatedly sz #(rand-nth alphabet))))

(comment
  (rand-str 10 ascii)
  ;;=> OMCIBULTOB
  )

(def rand-sym #(symbol (rand-str %1 %2)))
(def rand-key #(keyword (rand-str %1 %2)))

(comment
  (rand-key 10 ascii)
  ;;=> :JRFTYTUYQA
  
  (rand-sym 10 ascii)
  ;;=> DDHRWLOVME 
  )

(defn rand-vec [& generators]
  (into [] (map #(%) generators)))

(defn rand-map [sz kgen vgen]
  (into {}
    (repeatedly sz #(rand-vec kgen vgen))))

(comment
  (rand-vec #(rand-sym 5 ascii)
    #(rand-key 10 ascii)
    #(rand-int 1024))
  ;;=> [EGALM :FXTDTCMGRO 703]

  (rand-map 3 #(rand-key 5 ascii) #(rand-int 100))
  ;;=> {:RBBLD 94, :CQXLR 71, :LJQYL 72}
  )
