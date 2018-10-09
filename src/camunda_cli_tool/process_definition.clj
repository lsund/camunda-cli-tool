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
   (try
     (start-process! key (config/process-variables key))
     (catch Exception e
       (println "Could not start process:" (.getMessage e)))))
  ([key variables]
   (let [resp (http/rest-post (str rest-endpoint
                                   "/" "key"
                                   "/" key
                                   "/" "start")
                              variables)]
     {:value (:status resp)
      :rebound false})))

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
              "v" {:description "Start process instance with given variables"
                   :function read-variables-and-start-process!
                   :args [key]}
              "l" {:description "List Process Instances for this definition"
                   :next pinst/root
                   :args [id]}}})

(defn make-root [instances]
  {:title "Select Process Definition"
   :key "pd"
   :children instances})

(defn mergefun [{:keys [id key name] :as pdef}]
  (merge pdef {:description name
               :next manage :args [id key name]}))

(defn root []
  "Node for listing process definitions."
  (make-root (util/associate (constantly true) mergefun (list-most-recent))))
