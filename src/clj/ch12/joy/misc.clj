(ns joy.misc)

;;
;; Listing 12.14
;;
(.get '[a b c] 1)
;;=> b

(.get (repeat :a) 138)
;;=> :a

(.containsAll '[a b c] '[b c])
;;=> true

(.add '[a b c] 'd)
;; UnsupportedOperationException

(java.util.Collections/sort [3 4 2 1])
;; UnsupportedOperationException