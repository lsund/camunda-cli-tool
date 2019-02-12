(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.config :as config]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.process-instance :as pinst]
            [camunda-cli-tool.util :as util]
            [clojure.string :as string]
            [medley.core :refer [map-keys distinct-by]]))

(def rest-endpoint "process-definition")

(defn list-all []
  (if-let [body (:body (http/rest-get rest-endpoint))]
    (map (partial util/json->instance-map [:id :key :name :version])
         (j/read-str body))))

(defn list-most-recent []
  (if-let [body (:body (http/rest-get rest-endpoint {:latestVersion true}))]
    (map (partial util/json->instance-map [:id :key :name :version])
         (j/read-str body))))

(defn start-process!
  ([key]
   (start-process! key (config/process-variables key)))
  ([key variables]
   (try
     (let [resp (http/rest-post (str rest-endpoint
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
  (let [resp (http/rest-delete (str rest-endpoint "/" id) {:cascade true})]
    {:value (:status resp)
     :rebound true}))

(defmethod delete-process! :all [_ key]
  (let [resp (http/rest-delete (str rest-endpoint "/" "key" "/" key "/" "delete"))]
    {:value (:status resp)
     :rebound true}))

(defn read-variables-and-start-process! [key]
  (let [variables (atom {})]
    (doseq [k (config/required-keys key)]
      (println "Enter value for: " k)
      (let [v (read-line)]
        (swap! variables assoc k {:value v :type "String"})))
    (start-process! key @variables)))

(defn manage [id key name]
  "Node for managing a specific process definition."
  {:title (str "Manage Process Definition: " name)
   :children {"s" {:description "Start process instance with default variables"
                   :function start-process!
                   :args [key]}
              "v" {:description "Start process with given arguments"
                     :function start-process!
                     :args [key]}
              "pi" {:description "List Process Instances for this definition"
                    :next pinst/root
                    :args [id]}
              "d" {:description "Delete this process definition"
                   :function delete-process!
                   :args [:single id]}
              "da" {:description "Delete all process definition with this key"
                    :function delete-process!
                    :args [:all key]}}})

(defn make-root [instances]
  {:title "Select Process Definition"
   :key "pd"
   :children instances})

(defn mergefun [{:keys [id key name version] :as pdef}]
  (merge pdef {:description (str name " [version: " version "]")
               :next manage :args [id key name]}))

(defn root []
  "Node for listing process definitions."
  (make-root (util/associate (constantly true) mergefun (list-most-recent))))
