(ns camunda-cli-tool.util
  (:require
   [camel-snake-kebab.core :refer [->kebab-case]]
   [clojure.set :refer [rename-keys]]
   [medley.core :refer [map-keys]]))

(defn parse-int [s]
  (Integer. (re-find  #"\d+" (str s))))

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

(defn associate [pred mergefun xs]
  "Creates a sorted map of k/v pairs, where the key is a unique integer and the value is a map
   corresponding to the instance based on xs. mergefun is a function used for adding extra
  information to each instance. pred is a predicate function, that filters out
  elements, also based on xs"
  (into (sorted-map-by compare-parsed-int) (zipmap (map str (range))
                             (filter pred (map mergefun xs)))))

(defn json->instance-map [keyseq json]
  (rename-keys (select-keys (map-keys keyword json) keyseq)
               (zipmap keyseq (map ->kebab-case keyseq))))
