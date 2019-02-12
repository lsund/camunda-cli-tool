(ns camunda-cli-tool.decision-definition
  (:require [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [clojure.data.json :as j]))


(def rest-endpoint "decision-definition")

(defn list-all []
  (if-let [body (:body (http/rest-get rest-endpoint))]
    (map (partial util/json->instance-map [:id :key :name :version])
         (j/read-str body))))

(defn list-most-recent []
  (if-let [body (:body (http/rest-get rest-endpoint {:latestVersion true}))]
    (map (partial util/json->instance-map [:id :key :name :version])
         (j/read-str body))))

(defn handler [id key name]
  "Node for managing a specific process definition."
  {:title (str "Manage Decision Definition: " name)
   :children {"d" {:description "Delete this decision definition"
                   :function :todo
                   :args [id]}}})

(defn with-description-and-handler-fn [{:keys [id key name version] :as ddef}]
  (merge ddef {:description (str name " [version: " version "]")
               :handler-fn handler :handler-args [id key name]}))

(defn list-all []
  "Node for listing process definitions."
  {:title "Select Decision Definition"
   :key "dd"
   :children (util/build-indexed-map with-description-and-handler-fn (list-most-recent))})
