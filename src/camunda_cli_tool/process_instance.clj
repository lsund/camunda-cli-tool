(ns camunda-cli-tool.process-instance
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]))

(def padding-space 80)

(def rest-endpoint "process-instance")

(defrecord ProcessInstance [id definition-id])

(defn show [{:keys [id]}]
  (str id (util/padding-string id padding-space)))

(defn json->ProcessInstance [j]
  (select-keys (util/keywordize j) [:id :definitionId]))

(defn list-all []
  (map json->ProcessInstance (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn show-process-instances []
  (doseq [pinst (list-all)]
    (println (show pinst))))
