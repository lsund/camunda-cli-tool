(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [medley.core :refer [map-keys]]))

(def padding-space 69)

(def rest-endpoint "process-definition")

(defrecord ProcessDefinition [id key name version])

(defn show [{:keys [id version]}]
  (str id (util/padding-string id padding-space) " version: " version))

(defn json->ProcessDefinition [j]
  (select-keys (map-keys keyword j) [:id :key :name :version]))

(defn list-all []
  (map json->ProcessDefinition (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn list-unique []
  (sorted-set-by (fn [x y] (=  (:key x) (:key y))) (list-all)))

(defn show-unique-process-definitions []
  (doseq [pdef (list-all)]
    (println (show pdef))))
