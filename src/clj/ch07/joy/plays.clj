(ns joy.plays)

(def plays [{:band "Burial",     :plays 979,  :loved 9}
            {:band "Eno",        :plays 2333, :loved 15}
            {:band "Bill Evans", :plays 979,  :loved 9}
            {:band "Magma",      :plays 2665, :loved 31}])

(def sort-by-loved-ratio (partial sort-by #(/ (:plays %) (:loved %))))

(defn columns [column-names]
  (fn [row]
    (vec (map row column-names))))

(comment
  (sort-by-loved-ratio plays)
  ;;=> ({:band "Magma",      :plays 2665,  :loved 31}
  ;;    {:band "Burial",     :plays 979,   :loved 9}
  ;;    {:band "Bill Evans", :plays 979,   :loved 9}
  ;;    {:band "Eno",        :plays 2333,  :loved 15})

  (sort-by (columns [:plays :loved :band]) plays)
  ;;=> ({:band "Bill Evans", :plays 979,   :loved 9}
  ;;    {:band "Burial",     :plays 979,   :loved 9}
  ;;    {:band "Eno",        :plays 2333,  :loved 15}
  ;;    {:band "Magma",      :plays 2665,  :loved 31})
  )

(defn keys-apply [f ks m]
  (let [only (select-keys m ks)]
    (zipmap (keys only)
            (map f (vals only)))))

(defn manip-map [f ks m]
  (merge m (keys-apply f ks m)))

(defn mega-love! [ks]
  (map (partial manip-map #(int (* % 1000)) ks) plays))

(comment
  (keys-apply #(.toUpperCase %) #{:band} (plays 0))
  ;;=> {:band "BURIAL"}

  (manip-map #(int (/ % 2)) #{:plays :loved} (plays 0))
  ;;=> {:plays 489, :band "Burial", :loved 4}

  (mega-love! [:loved])
  ;;=> ({:plays 979,  :band "Burial",     :loved 9000}
  ;;    {:plays 2333, :band "Eno",        :loved 15000}
  ;;    {:plays 979,  :band "Bill Evans", :loved 9000}
  ;;    {:plays 2665, :band "Magma",      :loved 31000})
  )

