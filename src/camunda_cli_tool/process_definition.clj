(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]))

(def padding-space 69)

(def rest-endpoint "process-definition")

(defrecord ProcessDefinition [id key name version])

(defn show [{:keys [id version]}]
  (str id (util/padding-string id padding-space) " version: " version))

(defn json->ProcessDefinition [j]
  (select-keys (util/keywordize j) [:id :key :name :version]))

(defn list-all []
  (map json->ProcessDefinition (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn list-unique []
  ())

(defn show-unique-process-definitions []
  (doseq [pdef (list-all)]
    (println (show pdef))))
