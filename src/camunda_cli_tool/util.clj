(ns camunda-cli-tool.util)

(defn keywordize [m]
  (into {}
        (map vector
                (map keyword (keys m))
                (vals m))))
