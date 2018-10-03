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

(defn unlock! [id]
  {:value (:status (http/rest-post (str rest-endpoint
                                        "/" id
                                        "/" "unlock")))
   :rebound false})

(defn manage [id]
  {:title (str "Manage external task: " id)
   :children {"u" {:description "Unlock task" :function unlock! :args [id]}}})

(defn make-root [tasks]
  {:title "Inspect External Task"
   :key "et"
   :children tasks})

(defn mergefun [{:keys [id process-definition-id process-instance-id] :as task}]
  (merge task {:description id
               :next manage :args [id]}))

(defn root []
  (make-root (util/associate (constantly true) mergefun (list-all))))
