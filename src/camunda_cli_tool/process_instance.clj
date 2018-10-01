(ns camunda-cli-tool.process-instance
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]))

(def rest-endpoint "process-instance")

(defrecord ProcessInstance [id definition-id])

(defn show [pinst]
  (str (:id pinst)))

(defn keywordize [m]
  (into {}
        (map vector
                (map keyword (keys m))
                (vals m))))

(defn json->ProcessInstance [j]
  (select-keys (keywordize j) [:id]))

(defn list []
  (map json->ProcessInstance (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn show-process-instances []
  (doseq [x (list)]
    (println (:id x))))
