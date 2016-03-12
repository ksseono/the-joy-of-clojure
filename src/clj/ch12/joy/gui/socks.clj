;;
;; Listing 12.11
;;
(ns joy.gui.socks
  (:import
   (joy.gui DynaFrame)
   (javax.swing Box BoxLayout JTextField JPanel
     JSplitPane JLabel JButton
     JOptionPane)
   (java.awt BorderLayout Component GridLayout FlowLayout)
   (java.awt.event ActionListener)))

(defn shelf [& components]
  (let [shelf (JPanel.)]
    (.setLayout shelf (FlowLayout.))
    (doseq [c components] (.add shelf c))
    shelf))

(defn stack [& components]
  (let [stack (Box. BoxLayout/PAGE_AXIS)]
    (doseq [c components]
      (.setAlignmentX c Component/CENTER_ALIGNMENT)
      (.add stack c))
    stack))

(defn splitter [top bottom]
  (doto (JSplitPane.)
    (.setOrientation JSplitPane/VERTICAL_SPLIT)
    (.setLeftComponent top)
    (.setRightComponent bottom)))


;;
;; Listing 12.12
;;
(defn button [text f]
  (doto (JButton. text)
    (.addActionListener
      (proxy [ActionListener] []
        (actionPerformed [_] (f))))))

(defn txt [cols t]
  (doto (JTextField.)
    (.setColumns cols)
    (.setText t)))

(defn label [txt] (JLabel. txt))

(defn alert
  ([msg] (alert nil msg))
  ([frame msg]
   (javax.swing.JOptionPane/showMessageDialog frame msg)))

(comment
  (.display gui
            (splitter
             (button "Procrastinate" #(alert "Eat Cheetos"))
             (button "Move It" #(alert "Couch to 5k"))))
  )

(defn grid [x y f]
  (let [g (doto (JPanel.)
            (.setLayout (GridLayout. x y)))]
    (dotimes [i x]
      (dotimes [j y]
        (.add g (f))))
    g))

;;
;; Listing 12.13
;;
(comment
  (.display gui
            (let [g1 (txt 10 "Charlemagne")
                  g2 (txt 10 "Pippin")
                  r (txt 3 "10")
                  d (txt 3 "5")]
              (splitter
               (stack
                (shelf (label "Player 1") g1)
                (shelf (label "Player 2") g2)
                (shelf (label "Rounds ") r
                       (label "Delay  ") d))
               (stack
                (grid 21 11 #(label "-"))
                (button "Go!" #(alert (str (.getText g1) " vs. "
                                           (.getText g2) " for "
                                           (.getText r) " rounds, every "
                                           (.getText d) " seconds. ")))))))
  )
