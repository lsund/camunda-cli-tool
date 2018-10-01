(ns camunda-cli-tool.util)

(defn keywordize [m]
  (into {}
        (map vector
                (map keyword (keys m))
                (vals m))))

(defn padding-string [base length]
  (apply str (repeat (->> base count (- length)) \.)))
