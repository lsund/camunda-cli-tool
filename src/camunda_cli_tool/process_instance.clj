(ns camunda-cli-tool.process-instance
  (:require [clojure.data.json :as j]
            [clojure.set :refer [rename-keys]]
            [clojure.pprint :as pprint]
            [cheshire.core :as cheshire]
            [camunda-cli-tool.entity :as entity]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [camunda-cli-tool.external-task :as task]
            [medley.core :refer [map-keys]]))

(def rest-endpoint "process-instance")

(defn add-historic-data [pinst]
  (let [history (j/read-str (:body (http/rest-get (str "history"
                                                       "/" rest-endpoint
                                                       "/" (:id pinst)))))]
    (merge pinst
           {:start-time (get history "startTime")
            :definition-name (get history "processDefinitionName")})))

(defn active []
  (let [active (map (partial util/json->instance-map [:id :definitionId])
                    (j/read-str (:body (http/rest-get rest-endpoint))))]
    (map add-historic-data active)))

(defn stop-process! [id]
  {:value (:status (http/rest-delete (str rest-endpoint "/" id)))
   :rebound true})

(defn inspect-variables [id]
  (let [json (:body (http/rest-get (str rest-endpoint
                                        "/" id
                                        "/" "variables")))]
    {:value (str "\n" (pprint/write (cheshire/parse-string json true) :stream nil))
     :rebound false}))

(defn handler [{:keys [id]}]
  {:title (str "Inspect process: " id)
   :children {"s" {:description "Stop Process Instance" :function stop-process! :args [id]}
              "v" {:description "Inspect variables" :function inspect-variables :args [id]}
              "et" {:description "List external tasks for this instance "
                    :next task/list-all
                    :args [id]}}})

(def process-list-handler
  {:title "Inspect Process"
   :key "pi"})

(defn description [{:keys [id definition-name start-time]}]
  (str id " [" start-time "] " definition-name ))

(defn list-all
  "Node for listing process instances. If no arguments are given, return a node with all process
  instances. If one argument is given, then filter on process instances matching this
  definition-id."
  ([]
   (entity/all "Inspect Process"
               description
               handler
               (active)))
  ([definition-id]
   (entity/all "Inspect Process"
               description
               handler
               (active)
               #(= (:definition-id %) definition-id))))
