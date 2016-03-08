(ns joy.macros
  (:require [clojure.xml :as xml]))

(defn contextual-eval [ctx expr]
  (eval
    `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)]
       ~expr)))

(comment
  (contextual-eval '{a 1, b 2} '(+ a b))
  ;;=> 3

  (contextual-eval '{a 1, b 2} '(let [b 1000] (+ a b)))
  ;;=> 1001
  )


(defmacro domain [naem & body]
  `{:tag :domain,
    :attrs {:name (str '~name)},
    :content [~@body]})

(declare handle-things)

(defmacro grouping [name & body]
  `{:tag :grouping,
    :attrs {:name (str '~name)},
    :content [~@(handle-things body)]})

(declare grok-attrs grok-props)

(defn handle-things [things]
  (for [t things]
    {:tag :thing,
     :attrs (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c]
                [])}))

(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

(defn grok-props [props]
  (when props
    {:tag :properties, :attrs nil,
     :content (apply vector (for [p props]
                              {:tag :property,
                               :attrs {:name (str (first p))},
                               :content nil}))}))

(def d
  (domain man-vs-monster
          (grouping people
                    (Human "A stock human")
                    (Man (isa Human)
                         "A man, baby"
                         [name]
                         [has-beard?]))
          (grouping monsters
                    (Chupacabra
                     "A fierce, yet elusive creature"
                     [eats-goats?]))))

(comment
  (:tag d)
  ;;=> :domain

  (:tag (first (:content d)))
  ;;=> :grouping

  (xml/emit d)
  ;; <?xml version='1.0' encoding='UTF-8'?>
  ;; <domain name='clojure.core$name@1de49335'>
  ;;   <grouping name='people'>
  ;;     <thing name='Human' comment='A stock human'>
  ;;       <properties></properties>
  ;;     </thing>
  ;;     <thing name='Man' isa='Human' comment='A man, baby'>
  ;;       <properties>
  ;;         <property name='name'/>
  ;;         <property name='has-beard?'/>
  ;;       </properties>
  ;;     </thing>
  ;;   </grouping>
  ;;   <grouping name='monsters'>
  ;;     <thing name='Chupacabra' comment='A fierce, yet elusive creature'>
  ;;       <properties>
  ;;         <property name='eats-goats?'/>
  ;;       </properties>
  ;;     </thing>
  ;;   </grouping>
  ;; </domain>
  )
