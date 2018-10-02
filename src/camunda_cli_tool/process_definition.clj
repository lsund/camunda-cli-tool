(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [clojure.string :as string]
            [medley.core :refer [map-keys distinct-by]]))

(def padding-space 65)

(def rest-endpoint "process-definition")

(def default-variables {:fruit {:value "banana" :type "String"}})

(defrecord ProcessDefinition [id key name version])

(defn name->char [name taken]
  (->> (string/split name #"/")
       last
       string/lower-case
       util/filter-alphas
       (reduce (fn [acc c] (if (some #{c} taken) acc (conj acc c))) [])
       first))

(defn start-process! [key]
  (http/rest-post (str rest-endpoint
                       "/" "key"
                       "/" key
                       "/" "start")
                  default-variables))

(defn json->ProcessDefinition [j]
  (select-keys (map-keys keyword j) [:id :key :name :version]))

(defn list-all []
  (map json->ProcessDefinition (j/read-str (:body (http/rest-get rest-endpoint)))))

(defn list-most-recent []
  (->> (list-all)
       (sort-by :version >)
       (distinct-by (fn [{:keys [key]}] key))))

;; TODO has to update second argument of name->char
;; And check if name->char was nil. In that case assign an integer instead
(defn most-recent-keymap []
  (let [keymap (reduce
                (fn [acc {:keys [key name version]}]
                  (assoc acc
                         (name->char name [])
                         {:description name :function start-process! :args [key]}))
                {}
                (list-most-recent))]
    (assoc keymap :title "Start Process Definition")))
