(ns camunda-cli-tool.util)

(defn padding-string [base length]
  (apply str (repeat (->> base count (- length)) \.)))

(defn english-alphabet []
  (map char (take 26 (drop 97 (range)))))

(defn filter-alphas [s]
  (apply str (reduce (fn [acc c] (if (Character/isLetter c) (conj acc c) acc)) [] s)))

(defn int->char [n]
  (char (+ n 48)))
