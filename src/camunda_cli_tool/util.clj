(ns camunda-cli-tool.util
  (:require
   [camel-snake-kebab.core :refer [->kebab-case]]
   [clojure.set :refer [rename-keys]]
   [medley.core :refer [map-keys]]))

(defn parse-int [s]
  (try
    (Integer. (re-find  #"\d+" (str s)))
    (catch NumberFormatException e
      nil)))

(defn padding-string [base length]
  (apply str (repeat (->> base count (- length)) \.)))

(defn english-alphabet []
  (map char (take 26 (drop 97 (range)))))

(defn filter-alphas [s]
  (apply str (reduce (fn [acc c] (if (Character/isLetter c) (conj acc c) acc)) [] s)))

(defn int->char [n]
  (char (+ n 48)))

(defn compare-parsed-int [x y]
  (compare (parse-int x) (parse-int y)))

(defn build-indexed-map
  "Creates a sorted map of k/v pairs, where the key is a unique integer and the value is a map
   corresponding to the instance based on xs. `merged-with` is a function used for merging extra
  keys to each instance based on `xs`. `pred` is a predicate function, that filters out
  elements, also based on `xs`"
  ([merged-with xs]
   (build-indexed-map (constantly true) merged-with xs))
  ([pred merged-with xs]
   (into (sorted-map-by compare-parsed-int) (zipmap (map str (range))
                                                    (filter pred (map merged-with xs))))))

(defn json->instance-map [keyseq json]
  (rename-keys (select-keys (map-keys keyword json) keyseq)
               (zipmap keyseq (map ->kebab-case keyseq))))
