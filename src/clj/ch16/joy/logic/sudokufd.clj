(ns joy.logic.sudokufd
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.pldb :as pldb]
            [clojure.core.logic.fd :as fd])
  (:use [joy.sudoku]))

(defn rowify [board]
  (->> board
    (partition 9)
    (map vec)
    vec))

(defn colify [rows]
  (apply map vector rows))

(comment
  (colify (rowify b1))
  )

(defn subgrid [rows]
  (partition 9
    (for [row (range 0 9 3)
          col (range 0 9 3)
          x (range row (+ row 3))
          y (range col (+ col 3))]
      (get-in rows [x y]))))

(comment
  (subgrid (rowify b1))
  )

(def logic-board #(repeatedly 81 logic/lvar))

;;
;; Listing 16.9
;;
(defn init [[lv & lvs] [cell & cells]]
  (if lv
    (logic/fresh []
      (if (= '- cell)
        logic/succeed
        (logic/== lv cell))
      (init lvs cells))
    logic/succeed))

;;
;; Listing 16.10
;;
(defn solve-logically [board]
  (let [legal-nums (fd/interval 1 9)
        lvars (logic-board)
        rows (rowify lvars)
        cols (colify rows)
        grids (subgrid rows)]
    (logic/run 1 [q]
      (init lvars board)
      (logic/everyg #(fd/in % legal-nums) lvars)
      (logic/everyg fd/distinct rows)
      (logic/everyg fd/distinct cols)
      (logic/everyg fd/distinct grids)
      (logic/== q lvars))))

(comment
  (-> b1
    solve-logically
    first
    prep
    print-board)
  )
