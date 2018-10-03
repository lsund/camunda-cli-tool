(ns camunda-cli-tool.external-task
  (:require [clojure.data.json :as j]
            [clojure.set :refer [rename-keys]]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [medley.core :refer [map-keys]]))

(def rest-endpoint "external-task")

(defn list-all []
  (map (partial util/json->instance-map [:id :processDefinitionId :processInstanceId])
       (j/read-str (:body (http/rest-get rest-endpoint)))))
