(ns joy.sudoku
  (:require [clojure.set :as set])
  (:use [joy.persistent]))

(def b1 '[3 - - - - 5 - 1 -
          - 7 - - - 6 - 3 -
          1 - - - 9 - - - -
          7 - 8 - - - - 9 -
          9 - - 4 - 8 - - 2
          - 6 - - - - 5 - 1
          - - - - 4 - - - 6
          - 4 - 7 - - - 2 -
           - 2 - 6 - - - - 3])

(def b2 '[5 3 - - 7 - - - -
          6 - - 1 9 5 - - -
          - 9 8 - - - - 6 -
          8 - - - 6 - - - 3
          4 - - 8 - 3 - - 1
          7 - - - 2 - - - 6
          - 6 - - - - 2 8 -
          - - - 4 1 9 - - 5
          - - - - 8 - - 7 9])

(defn prep [board]
  (map #(partition 3 %)
    (partition 9 board)))


;;
;; Listing 16.1
;;
(defn print-board [board]
  (let [row-sep (apply str (repeat 37 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (print "| ")
      (doseq [subrow (nth board row)]
        (doseq [cell (butlast subrow)]
          (print (str cell "   ")))
        (print (str (last subrow) " | ")))    
    (println)
    (when (zero? (mod (inc row) 3))
      (println row-sep)))))

(comment
  (-> b1 prep print-board)
  ;; -------------------------------------
  ;; | 3   -   - | -   -   5 | -   1   - | 
  ;; | -   7   - | -   -   6 | -   3   - | 
  ;; | 1   -   - | -   9   - | -   -   - | 
  ;; -------------------------------------
  ;; | 7   -   8 | -   -   - | -   9   - | 
  ;; | 9   -   - | 4   -   8 | -   -   2 | 
  ;; | -   6   - | -   -   - | 5   -   1 | 
  ;; -------------------------------------
  ;; | -   -   - | -   4   - | -   -   6 | 
  ;; | -   4   - | 7   -   - | -   2   - | 
  ;; | -   2   - | 6   -   - | -   -   3 | 
  ;; -------------------------------------
  )

(defn rows [board sz]
  (partition sz board))

(defn row-for [board index sz]
  (nth (rows board sz) (/ index 9)))

(defn column-for [board index sz]
  (let [col (mod index sz)]
    (map #(nth % col)
      (rows board sz))))

(defn subgrid-for [board i]
  (let [rows (rows board 9)
        sgcol (/ (mod i 9) 3)
        sgrow (/ (/ i 9) 3)
        grp-col (column-for (mapcat #(partition 3 %) rows) sgcol 3)
        grp (take 3 (drop (* 3 (int sgrow)) grp-col))]
    (flatten grp)))

(defn numbers-present-for [board i]
  (set
    (concat (row-for board i 9)
      (column-for board i 9)
      (subgrid-for board i))))

(defn possible-placements [board index]
  (set/difference #{1 2 3 4 5 6 7 8 9}
    (numbers-present-for board index)))


;;
;; Listing 16.2
;;
(defn solve [board]
  (if-let [[i & _]
           (and (some '#{-} board)
             (pos '#{-} board))]
    (flatten (map #(solve (assoc board i %))
               (possible-placements board i)))
    board))

(comment
  (-> b1
    solve
    prep
    print-board)
  ;; -------------------------------------
  ;; | 3   8   6 | 2   7   5 | 4   1   9 | 
  ;; | 4   7   9 | 8   1   6 | 2   3   5 | 
  ;; | 1   5   2 | 3   9   4 | 8   6   7 | 
  ;; -------------------------------------
  ;; | 7   3   8 | 5   2   1 | 6   9   4 | 
  ;; | 9   1   5 | 4   6   8 | 3   7   2 | 
  ;; | 2   6   4 | 9   3   7 | 5   8   1 | 
  ;; -------------------------------------
  ;; | 8   9   3 | 1   4   2 | 7   5   6 | 
  ;; | 6   4   1 | 7   5   3 | 9   2   8 | 
  ;; | 5   2   7 | 6   8   9 | 1   4   3 | 
  ;; -------------------------------------

  (-> b2
    solve
    prep
    print-board)
  ;; -------------------------------------
  ;; | 5   3   4 | 6   7   8 | 9   1   2 | 
  ;; | 6   7   2 | 1   9   5 | 3   4   8 | 
  ;; | 1   9   8 | 3   4   2 | 5   6   7 | 
  ;; -------------------------------------
  ;; | 8   5   9 | 7   6   1 | 4   2   3 | 
  ;; | 4   2   6 | 8   5   3 | 7   9   1 | 
  ;; | 7   1   3 | 9   2   4 | 8   5   6 | 
  ;; -------------------------------------
  ;; | 9   6   1 | 5   3   7 | 2   8   4 | 
  ;; | 2   8   7 | 4   1   9 | 6   3   5 | 
  ;; | 3   4   5 | 2   8   6 | 1   7   9 | 
  ;; -------------------------------------
  )
