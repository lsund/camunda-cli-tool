(ns camunda-cli-tool.process-definition
  (:require [clojure.data.json :as j]
            [camunda-cli-tool.http :as http]
            [camunda-cli-tool.util :as util]
            [clojure.string :as string]
            [medley.core :refer [map-keys distinct-by]]))

(def padding-space 65)

(def rest-endpoint "process-definition")

(defrecord ProcessDefinition [id key name version])

(defn name->char [name taken]
  (->> (string/split name #"/")
       last
       string/lower-case
       util/remove-whitespace
       (reduce (fn [acc c] (if (some #{c} taken) acc (conj acc c))) [])
       first))

(defn json->ProcessDefinition [j]
  (select-keys (map-keys keyword j) [:id :key :name :version]))

(defn list-all []
  (map json->ProcessDefinition (j/read-str (:body (http/make-rest-call rest-endpoint)))))

(defn list-most-recent []
  (->> (list-all)
       (sort-by :version >)
       (distinct-by (fn [{:keys [key]}] key))))

;; TODO has to update second argument of name->char
(defn most-recent-keymap []
  (reduce
   (fn [acc {:keys [name version]}]
     (assoc acc (name->char name []) {:description name :command nil}))
   {}
   (list-most-recent)))
