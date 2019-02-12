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
  {:value (:status (http/rest-post (str rest-endpoint "/" id "/" "unlock") {}))
   :rebound false})

(defn element [id]
  "Node for managing a specific external task"
  {:title (str "Manage external task: " id)
   :children {"u" {:description "Unlock task" :function unlock! :args [id]}}})

(defn with-description-and-handler-fn [{:keys [id] :as task}]
  (merge task {:description id
               :manage-fn element
               :manage-args [id]}))

(def external-task-list-handler
  {:title "Inspect External Task"
   :key "et"})

(defn list-all
  "Node for listing external tasks"
  ([]
   (assoc external-task-list-handler :children (util/build-indexed-map with-description-and-handler-fn (list-all))))
  ([pinst-id]
   (assoc external-task-list-handler
          :children
          (util/associate #(= (:process-instance-id %) pinst-id)
                          with-description-and-handler-fn
                          (list-all)))))
