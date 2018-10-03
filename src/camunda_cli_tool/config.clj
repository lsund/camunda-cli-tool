(ns camunda-cli-tool.config
  (:require [clojure.edn :as edn]))

(defn load-default-variables [key]
  (if-let [variables (get-in (edn/read-string (slurp "resources/edn/process-variables.edn"))
                             [key :default])]
    variables
    (throw (Exception. "No configured variables for process"))))

(defn required-keys [key]
  (if-let [keys (get-in (edn/read-string (slurp "resources/edn/process-variables.edn"))
                        [key :required])]
    keys
    (throw (Exception. "No configured required keys for process"))))
