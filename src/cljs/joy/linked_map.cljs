;;
;; Listing 13.1
;;
(ns joy.linked-map
  (:require [goog.structs.LinkedMap]))

(extend-type goog.structs.LinkedMap
  cljs.core/ICounted
  (-count [m] (.getCount m)))

(def m (goog.structs.LinkedMap.))

(comment
  (count m)
  ;;=> 0
  )

(.set m :foo :bar)
(.set m :baz :qux)

(comment
  (count m)
  ;;=> 2
  )

;;
;; Listing 13.2
;;
(.set m 43 :odd)

(comment
  (m 43)
  ;; org.mozilla.javascript.EcmaError:
  ;;     TypeError: Cannot find function call in object [object Object]
  )

(extend-type goog.structs.LinkedMap
  cljs.core/IFn
  (-invoke
    ([m k] (.get m k nil))
    ([m k not-found] (.get m k not-found))))

(comment
  (m 43)
  ;;=> :odd
  )
