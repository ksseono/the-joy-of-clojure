(ns joy.treenode)

;;
;; Listing 9.1
;;
(defrecord TreeNode [val l r])

(defn xconj [t v]
  (cond
    (nil? t)       (TreeNode. v nil nil)
    (< v (:val t)) (TreeNode. (:val t) (xconj (:l t) v) (:r t))
    :else          (TreeNode. (:val t) (:l t) (xconj (:r t) v))))

(defn xseq [t]
  (when t
    (concat (xseq (:l t)) [(:val t)] (xseq (:r t)))))

(def sample-tree (reduce xconj nil [3 5 2 4 6]))

(comment
  (xseq sample-tree)
  ;;=> (2 3 4 5 6)

  (dissoc (TreeNode. 5 nil nil) :l)
  ;;=> {:val 5, :r nil}
  )

(defprotocol FIXO
  (fixo-push [fixo value])
  (fixo-pop [fixo])
  (fixo-peek [fixo]))

(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (xconj node value)))

(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vector value]
    (conj vector value)))

(extend-type nil
  FIXO
  (fixo-push [t v]
    (TreeNode. v nil nil)))

(comment
  (xseq (fixo-push sample-tree 5/2))
  ;;=> (2 5/2 3 4 5 6)

  (fixo-push [2 3 4 5 6] 5/2)
  ;;=> [2 3 4 5 6 5/2]

  (xseq (reduce fixo-push nil [3 5 2 4 6 0]))
  ;;=> (0 2 3 4 5 6)
  )


;;
;; Listing 9.2
;;
(extend-type TreeNode
  FIXO
  (fixo-push [node value]
    (xconj node value))
  (fixo-peek [node]
    (if (:l node)
      (recur (:l node))
      (:val node)))
  (fixo-pop [node]
    (if (:l node)
      (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
      (:r node))))

(extend-type clojure.lang.IPersistentVector
  FIXO
  (fixo-push [vector value]
    (conj vector value))
  (fixo-peek [vector]
    (peek vector))
  (fixo-pop [vector]
    (pop vector)))

(defn fixo-into [c1 c2]
  (reduce fixo-push c1 c2))

(comment
  (xseq (fixo-into (TreeNode. 5 nil nil) [2 4 6 7]))
  ;;=> (2 4 5 6 7)

  (seq (fixo-into [5] [2 4 6 7]))
  ;;=> (5 2 4 6 7)
  )


;;
;; Listing 9.3
;;
(def tree-node-fixo
  {:fixo-push (fn [node value]
                (xconj node value))
   :fixo-peek (fn [node]
                (if (:l node)
                  (recur (:l node))
                  (:val node)))
   :fixo-pop (fn [node]
               (if (:l node)
                 (TreeNode. (:val node) (fixo-pop (:l node)) (:r node))
                 (:r node)))})

(extend TreeNode FIXO tree-node-fixo)

(comment
  (xseq (fixo-into (TreeNode. 5 nil nil) [2 4 6 7]))
  ;;=> (2 4 5 6 7)
  )


;;
;; Listing 9.4
;;
(defn fixed-fixo
  ([limit] (fixed-fixo limit []))
  ([limit vector]
   (reify FIXO
     (fixo-push [this value]
       (if (< (count vector) limit)
         (fixed-fixo limit (conj vector value))
         this))
     (fixo-peek [_]
       (peek vector))
     (fixo-pop [_]
       (pop vector)))))


;;
;; Listing 9.5
;;
(defrecord TreeNode [val l r]
  FIXO
  (fixo-push [t v]
    (if (< v val)
      (TreeNode. val (fixo-push l v) r)
      (TreeNode. val l (fixo-push r v))))
  (fixo-peek [t]
    (if l
      (fixo-peek l)
      val))
  (fixo-pop [t]
    (if l
      (TreeNode. val (fixo-pop l) r)
      r)))

(def sample-tree2 (reduce fixo-push (TreeNode. 3 nil nil) [5 2 4 6]))
(comment
  (xseq sample-tree2)
  ;;=> (2 3 4 5 6)
  )


;;
;; Listing 9.6
;;
(deftype TreeNode [val l r]
  FIXO
  (fixo-push [_ v]
    (if (< v val)
      (TreeNode. val (fixo-push l v) r)
      (TreeNode. val l (fixo-push r v))))
  (fixo-peek [_]
    (if l
      (fixo-peek l)
      val))
  (fixo-pop [_]
    (if l
      (TreeNode. val (fixo-pop l) r)
      r))

  clojure.lang.IPersistentStack
  (cons [this v] (fixo-push this v))
  (peek [this] (fixo-peek this))
  (pop [this] (fixo-pop this))

  clojure.lang.Seqable
  (seq [t]
    (concat (seq l) [val] (seq r))))

(extend-type nil
  FIXO
  (fixo-push [t v]
    (TreeNode. v nil nil)))

(def sample-tree3 (into (TreeNode. 3 nil nil) [5 2 4 6]))

(comment
  (seq sample-tree3)
  ;;=> (2 3 4 5 6)
  )
