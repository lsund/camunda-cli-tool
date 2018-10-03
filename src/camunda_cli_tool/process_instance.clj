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

(defn add-start-time [pinst]
  (assoc pinst
             :start-time
             (get (j/read-str (:body (http/rest-get (str "history"
                                                         "/" rest-endpoint
                                                         "/" (:id pinst))))) "startTime")))

(defn list-all []
  (let [active (map json->ProcessInstance (j/read-str (:body (http/rest-get rest-endpoint))))]
    (map add-start-time active)))

(defn stop-process! [id]
  {:value (:status (http/rest-delete (str rest-endpoint "/" id)))
   :rebound true})

(defn inspect-variables [id]
  {:value (:body (http/rest-get (str rest-endpoint
                                     "/" id
                                     "/" "variables")))
   :rebound false})

(defn manage [id]
  {:title "Manage Process Instance"
   :children {"s" {:description "Stop Process Instance" :function stop-process! :args [id]}
              "v" {:description "Inspect variables" :function inspect-variables :args [id]}}})

(defn make-root [instances]
  {:title "Inspect Process"
   :key "pi"
   :children instances})

(defn mergefun [{:keys [id start-time] :as pinst}]
  (merge pinst {:description (str id " [" start-time "]")
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
