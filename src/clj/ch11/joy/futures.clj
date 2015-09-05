(ns joy.futures
  "section 11"
  (:require (clojure [xml :as xml]))
  (:require (clojure [zip :as zip]))
  (:import (java.util.regex Pattern)))

(defn feed->zipper [uri-str]
  (->> (xml/parse uri-str)
    zip/xml-zip))

(defn normalize [feed]
  (if (= :feed (:tag (first feed)))
    feed
    (zip/down feed)))

(defn feed-children [uri-str]
  (->> uri-str
    feed->zipper
    normalize
    zip/children
    (filter (comp #{:item :entry} :tag))))

(defn title [entry]
  (some->> entry
    :content
    (some #(when (= :title (:tag %)) %))
    :content
    first))

(defn count-text-task [extractor txt feed]
  (let [items (feed-children feed)
        re (Pattern/compile (str "(?i)" txt))]
    (->> items
      (map extractor)
      (mapcat #(re-seq re %))
      count)))

(def feeds #{"http://feeds.feedburner.com/ElixirLang"
             "http://blog.fogus.me/feed/"})

(let [results (for [feed feeds]
                (future
                  (count-text-task title "Elixir" feed)))]
  (reduce + (map deref results)))

(defmacro as-futures [[a args] & body]
  (let [parts (partition-by #{'=>} body)
        [acts _ [res]] (partition-by #{:as} (first parts))
        [_ _ task] parts]
    `(let [~res (for [~a ~args] (future ~@acts))]
       ~@task)))

(defn occurrences [extractor tag & feeds]
  (as-futures [feed feeds]
    (count-text-task extractor tag feed)
    :as results
    =>
    (reduce + (map deref results))))


(comment
  (count-text-task
    title
    "Erlang"
    "http://feeds.feedburner.com/ElixirLang")

  (count-text-task
    title
    "Elixir"
    "http://feeds.feedburner.com/ElixirLang")

  (occurrences title "released"
    "http://blog.fogus.me/feed/"
    "http://feeds.feedburner.com/ElixirLang"
    "http://feeds.feedburner.com/kotlin")
)


