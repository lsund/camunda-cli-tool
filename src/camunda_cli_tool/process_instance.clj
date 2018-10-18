(ns camunda-cli-tool.process-instance
  (:require [clojure.data.json :as j]
            [clojure.set :refer [rename-keys]]
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

(defn list-all []
  (let [active (map (partial util/json->instance-map [:id :definitionId])
                    (j/read-str (:body (http/rest-get rest-endpoint))))]
    (map add-historic-data active)))

(defn stop-process! [id]
  {:value (:status (http/rest-delete (str rest-endpoint "/" id)))
   :rebound true})

(defn inspect-variables [id]
  {:value (:body (http/rest-get (str rest-endpoint
                                     "/" id
                                     "/" "variables")))
   :rebound false})

(defn manage [id desc]
  "Node for managing a specific process instance."
  {:title (str "Manage Process Instance: " desc)
   :children {"s" {:description "Stop Process Instance" :function stop-process! :args [id]}
              "v" {:description "Inspect variables" :function inspect-variables :args [id]}
              "et" {:description "List external tasks for this instance "
                   :next task/root
                   :args [id]}}})

(defn make-root [instances]
  {:title "Inspect Process"
   :key "pi"
   :children instances})

(defn mergefun [{:keys [id definition-name start-time] :as pinst}]
  (let [desc (str id " [" start-time "] " definition-name )]
    (merge pinst {:description desc
                  :next manage :args [id desc]})))

(defn root
  "Node for listing process instances. If no arguments are given, return a node with all process
  instances.  If one argument is given, then filter on process instances matching this
  definition-id."
  ([]
   (make-root (util/associate (constantly true)
                              mergefun
                              (list-all))))
  ([definition-id]
   (make-root (util/associate (fn [pinst] (= (:definition-id pinst) definition-id))
                              mergefun
                              (list-all)))))
