(ns joy.udp
  (:refer-clojure :exclude [get]))

(defn beget [this proto]
  (assoc this ::prototype proto))

(defn get [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

(def put assoc)

(comment
  (beget {:sub 0} {:super 1})
  ;;=> {:sub 0, :joy.udp/prototype {:super 1}}

  (get (beget {:sub 0} {:super 1})
       :super)
  ;;=> 1
  )


;;
;; cat
;;
(def cat {:likes-dogs true, :ocd-bathing true})
(def morris (beget {:likes-9lives true} cat))
(def post-traumatic-morris (beget {:likes-dogs nil} morris))

(comment
  (get cat :likes-dogs)
  ;;=> true

  (get morris :likes-dogs)
  ;;=> true

  (get post-traumatic-morris :likes-dogs)
  ;;=> nil

  (get post-traumatic-morris :likes-9lives)
  ;;=> true
  )


;;
;; compiler
;;
(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx  [m] (get m :llvm-compiler))

(def clone (partial beget {}))
(def unix {:os ::unix, :c-compiler "cc", :home "/home", :dev "/dev"})
(def osx (-> (clone unix)
             (put :os ::osx)
             (put :llvm-compiler "clang")
             (put :home "/Users")))

(comment
  (compiler unix)
  ;;=> "cc"

  (compiler osx)
  ;;=> "clang"
  )

(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(comment
  (home unix)
  ;;=> "/home"

  (home osx)
  ;; IllegalArgumentException
  ;;   No method in multimethod 'home' for dispatch value: :joy.udp/osx
  )

(derive ::osx ::unix)
(comment
  (home osx)
  ;;=> "/Users"

  (parents ::osx)
  ;;=> #{:joy.udp/unix}
  
  (ancestors ::osx)
  ;;=> #{:joy.udp/unix}
  
  (descendants ::unix)
  ;;=> #{:joy.udp/osx}

  (isa? ::osx ::unix)
  ;;=> true

  (isa? ::unix ::osx)
  ;;=> false
  )

(derive ::osx ::bsd)
(defmethod home ::bsd [m] "/home")
(comment
  (home osx)
  ;; IllegalArgumentException Multiple methods in multimethod
  ;;  'home' match dispatch value: :joy.udp/osx -> :joy.udp/bsd and
  ;;  :joy.udp/unix, and neither is preferred
  )

(prefer-method home ::unix ::bsd)
(comment
  (home osx)
  ;;=> "/Users"
  )

(remove-method home ::bsd)
(comment
  (home osx)
  ;;=> "/Users"

  (derive (make-hierarchy) ::osx ::unix)
  ;;=> {:parents {:joy.udp/osx #{:joy.udp/unix}},
  ;;     :ancestors {:joy.udp/osx #{:joy.udp/unix}},
  ;;     :descendants {:joy.udp/unix #{:joy.udp/osx}}}
  )

(defmulti compile-cmd (juxt :os compiler))

(defmethod compile-cmd [::osx "clang"] [m]
  (str "/usr/bin/" (get m :c-compiler)))

(defmethod compile-cmd :default [m]
  (str "Unsure where to locate " (get m :c-compiler)))

(comment
  (compile-cmd osx)
  ;;=> "/usr/bin/cc"

  (compile-cmd unix)
  ;;=> "Unsure where to locate cc"
  )
