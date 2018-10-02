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
  (rename-keys  (select-keys (map-keys keyword j) [:id :definitionId]) {:definitionId :definition-id}))

(defn list-all []
  (map json->ProcessInstance (j/read-str (:body (http/rest-get rest-endpoint)))))

(defn make-root [instances]
  {:title "Inspect Process"
   :children instances})

(defn associate [pred]
  "Creates a sorted map of [i m] k/v pairs, where i is a unique integer and m is the map that
   corresponds to the process instance. pred is a predicate function that is used for filtering
   elements based on (list-all)"
  (into (sorted-map) (zipmap (range)
                              (filter pred (map #(assoc % :description (:id %)) (list-all))))))

(defn root
  "If no arguments are given, return a node with all process instances.
   If one argument is given, then filter on process instances matching this definition-id."
  ([]
   (make-root (associate (constantly true))))
  ([definition-id]
   (make-root (associate (fn [inst] (= (:definition-id inst) definition-id))))))
