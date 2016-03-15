(ns joy.logic.unify
  (:require [clojure.walk :as walk]))

;;
;; Listing 16.3
;;
(defn lvar?
  "Determines if a value represents a logic variable"
  [x]
  (boolean
    (when (symbol? x)
      (re-matches #"^\?.*" (name x)))))

(comment
  (lvar? '?x)
  ;;=> true
  
  (lvar? 'a)
  ;;=> false
  
  (lvar? 2)
  ;;=> false
  )


;;
;; Listing 16.4
;;
(defn satisfy1
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      :default nil)))

(comment
  (satisfy1 '?something 2 {})
  ;;=> {?something 2}
  
  (satisfy1 2 '?something {})
  ;;=> {?something 2}

  (->> {}
    (satisfy1 '?x '?y)
    (satisfy1 '?x 1))
  ;;=> {?x ?y, ?y 1}
  )


;;
;; Listing 16.5
;;
(defn satisfy
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (not knowledge) nil
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      (every? seq? [L R])
      (satisfy (rest L)
        (rest R)
        (satisfy (first L)
          (first R)
          knowledge))
      :default nil)))

(comment
  (satisfy '(1 2 3) '(1 ?something 3) {})
  ;;=> {?something 2}

  (satisfy '((((?something)))) '((((2)))) {})
  ;;=> {?something 2}

  (satisfy '(?x 2 3 (4 5 ?z))
    '(1 2 ?y (4 5 6))
    {})
  ;;=> {?x 1, ?y 3, ?z 6}

  (satisfy '?x '(?y) {})
  ;;=> {?x (?y)}

  (satisfy '(?x 10000 3) '(1 2 ?y) {})
  ;;=> nil
  )


;;
;; Listing 16.6
;;
(require '[clojure.walk :as walk])

(defn subst [term binds]
  (walk/prewalk
    (fn [expr]
      (if (lvar? expr)
        (or (binds expr) expr)
        expr))
    term))

(comment
  (subst '(1 ?x 3) '{?x 2})
  ;;=> (1 2 3)

  (subst '((((?x)))) '{?x 2})
  ;;=> ((((2))))

  (subst '[1 ?x 3] '{?x 2})
  ;;=> [1 2 3]

  (subst '{:a ?x, :b [1 ?x 3]} '{?x 2})
  ;;=> {:a 2, :b [1 2 3]}

  (subst '(1 ?x 3) '{})
  ;;=> (1 ?x 3)

  (subst '(1 ?x 3) '{?x ?y})
  ;;=> (1 ?y 3)

  (def page
    '[:html
      [:head [:title ?title]]
      [:body [:h1 ?title]]])

  (subst page '{?title "Hi!"})
  ;;=> [:html [:head [:title "Hi!"]] [:body [:h1 "Hi!"]]]
  )


;;
;; Listin 16.7
;;
(defn meld [term1 term2]
  (->> {}
    (satisfy term1 term2)
    (subst term1)))

(comment
  (meld '(1 ?x 3) '(1 2 ?y))
  ;;=> (1 2 3)

  (meld '(1 ?x) '(?y (?y 2)))
  ;;=> (1 (1 2))

  (satisfy '?x 1 (satisfy '?x '?y {}))
  ;;=> {?x ?y, ?y 1}

  (satisfy '(1 ?x) '(?y (?y 2)) {})
  ;;=> {?y 1, ?x (?y 2)}
  )
