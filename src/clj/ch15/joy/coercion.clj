(ns joy.coercion)

;;
;; Listing 15.5
;;
(defn factorial-a [original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (time (dotimes [_ 1e5] (factorial-a 20))))

;;
;; Listing 15.6
;;
(defn factorial-b [original-x]
  (loop [x (long original-x), acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (time (dotimes [_ 1e5] (factorial-b 20))))

;;
;; Listing 15.7
;;
(defn factorial-c [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (time (dotimes [_ 1e5] (factorial-c 20))))

;;
;; Listing 15.8
;;
(set! *unchecked-math* true)

(defn factorial-d [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(set! *unchecked-math* false)

(comment
  (time (dotimes [_ 1e5] (factorial-d 20))))

;;
;; Listing 15.9
;;
(defn factorial-e [^double original-x]
  (loop [x original-x, acc 1.0]
    (if (>= 1.0 x)
      acc
      (recur (dec x) (* x acc)))))

(comment
  (factorial-e 10.0)
  (factorial-e 20.0)
  (factorial-e 30.0)
  (factorial-e 171.0)
  (time (dotimes [_ 1e5] (factorial-e 20.0))))

;;
;; Listing 15.10
;;
(defn factorial-f [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (*' x acc)))))

(comment
  (factorial-f 20)
  (factorial-f 30)
  (factorial-f 171)
  (time (dotimes [_ 1e5] (factorial-f 20))))
