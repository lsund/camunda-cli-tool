(ns camunda-cli-tool.process-instance
  (:require [clojure.data.json :as j]
            [clojure.set :refer [rename-keys]]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [medley.core :refer [map-keys]]))

(def padding-space 80)

(def rest-endpoint "process-instance")

(defrecord ProcessInstance [id definition-id])

(defn show [{:keys [id]}]
  (str id (util/padding-string id padding-space)))

(defn json->ProcessInstance [j]
  (rename-keys  (select-keys (map-keys keyword j) [:id :definitionId])
                {:definitionId :definition-id}))

(defn add-historic-data [pinst]
  (let [history (j/read-str (:body (http/rest-get (str "history"
                                                       "/" rest-endpoint
                                                       "/" (:id pinst)))))]
    (merge pinst
           {:start-time (get history "startTime")
            :definition-name (get history "processDefinitionName")})))

(defn list-all []
  (let [active (map json->ProcessInstance (j/read-str (:body (http/rest-get rest-endpoint))))]
    (map add-historic-data active)))

(defn stop-process! [id]
  {:value (:status (http/rest-delete (str rest-endpoint "/" id)))
   :rebound true})

(defn inspect-variables [id]
  {:value (:body (http/rest-get (str rest-endpoint
                                     "/" id
                                     "/" "variables")))
   :rebound false})

(defn manage [id]
  {:title (str "Manage Process Instance: " id)
   :children {"s" {:description "Stop Process Instance" :function stop-process! :args [id]}
              "v" {:description "Inspect variables" :function inspect-variables :args [id]}}})

(defn make-root [instances]
  {:title "Inspect Process"
   :key "pi"
   :children instances})

(defn mergefun [{:keys [id definition-name start-time] :as pinst}]
  (merge pinst {:description (str definition-name ": " id " [" start-time "]")
                :next manage :args [id]}))

(defn root
  "If no arguments are given, return a node with all process instances.
   If one argument is given, then filter on process instances matching this definition-id."
  ([]
   (make-root (util/associate (constantly true)
                              mergefun
                              (list-all))))
  ([definition-id]
   (make-root (util/associate (fn [pinst] (= (:definition-id pinst) definition-id))
                              mergefun
                              (list-all)))))
