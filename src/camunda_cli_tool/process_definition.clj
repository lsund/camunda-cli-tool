(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]))

(def padding-space 69)

(def rest-endpoint "process-definition")

(defrecord ProcessDefinition [id key name version])

(defn show [pdef]
  (let [padding (apply str (repeat (- 69 (count (:id pdef))) \. ))]
    (str (:id pdef) padding " version: " (:version pdef))))

(defn json->ProcessDefinition [j]
  (select-keys (util/keywordize j) [:id :key :name :version]))

(defn list []
  (map json->ProcessDefinition (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn show-unique-process-definitions []
  (doseq [pdef (list)]
    (println (show pdef))))
