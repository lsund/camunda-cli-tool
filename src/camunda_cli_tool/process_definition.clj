(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.config :as config]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.process-instance :as pinst]
            [camunda-cli-tool.util :as util]
            [camunda-cli-tool.entity :as entity]
            [clojure.string :as string]
            [medley.core :refer [map-keys distinct-by]]))

(defn most-recent []
  (if-let [body (:body (http/rest-get "process-definition" {:latestVersion true}))]
    (map (partial util/json->instance-map [:id :key :name :version])
         (j/read-str body))))

(defn start-process!
  ([key]
   (start-process! key (config/process-variables key)))
  ([key variables]
   (try
     (let [resp (http/rest-post (str "process-definition"
                                     "/" "key"
                                     "/" key
                                     "/" "start")
                                variables)]
       {:value (:status resp)
        :rebound false})
     (catch Exception e
       (println "Could not start process:" (.getMessage e))))))

(defmulti delete-process! first)

(defmethod delete-process! :single [_ id]
  (let [resp (http/rest-delete (str "process-definition") {:cascade true})]
    {:value (:status resp)
     :rebound true}))

(defmethod delete-process! :all [_ key]
  (let [resp (http/rest-delete (str "process-definition"))]
    {:value (:status resp)
     :rebound true}))

(defn read-variables-and-start-process! [key]
  (let [variables (atom {})]
    (doseq [k (config/required-keys key)]
      (println "Enter value for: " k)
      (let [v (read-line)]
        (swap! variables assoc k {:value v :type "String"})))
    (start-process! key @variables)))

(defn handler [{:keys [id key name]}]
  "Node for managing a specific process definition."
  {:title (str "Manage " name)
   :children {"s" {:description "Start process instance with default variables"
                   :function start-process!
                   :args [key]}
              "v" {:description "Start process with given arguments"
                   :function start-process!
                   :args [key]}
              "pi" {:description "List Process Instances for this definition"
                    :next pinst/list-all
                    :args [id]}
              "d" {:description "Delete this process definition"
                   :function delete-process!
                   :args [:single id]}
              "da" {:description "Delete all process definition with this key"
                    :function delete-process!
                    :args [:all key]}}})

(defn description [{:keys [name version]}]
  (str name " [version: " version "]"))

(defn list-all []
  "Node for listing process definitions."
  (entity/all "Select Process Definition"
               description
               handler
               most-recent))
