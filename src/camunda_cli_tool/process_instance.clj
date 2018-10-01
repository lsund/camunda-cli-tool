(ns camunda-cli-tool.process-instance
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]))

(def rest-endpoint "process-instance")

(defrecord ProcessInstance [id definition-id])

(defn show [pinst]
  (str (:id pinst)))

(defn json->ProcessInstance [j]
  (select-keys (util/keywordize j) [:id :definitionId]))

(defn list []
  (map json->ProcessInstance (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn show-process-instances []
  (doseq [pinst (list)]
    (println (show pinst))))
