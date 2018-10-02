(ns camunda-cli-tool.util)

(defn padding-string [base length]
  (apply str (repeat (->> base count (- length)) \.)))
