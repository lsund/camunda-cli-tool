(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [medley.core :refer [map-keys distinct-by]]))

(def padding-space 69)

(def rest-endpoint "process-definition")

(defrecord ProcessDefinition [id key name version])

(defn show [{:keys [id name version]}]
  (str name (util/padding-string name padding-space) " version: " version))

(defn json->ProcessDefinition [j]
  (select-keys (map-keys keyword j) [:id :key :name :version]))

(defn list-all []
  (map json->ProcessDefinition (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn list-most-recent []
  (->> (list-all)
       (sort-by :version)
       reverse
       (distinct-by (fn [{:keys [key]}] key))))

(defn show-most-recent-definitions []
  (doseq [pdef (list-most-recent)]
    (println (show pdef))))
