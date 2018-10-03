(ns camunda-cli-tool.config
  (:require [clojure.edn :as edn]))

(defn load-variables [key]
  (if-let [variables (get (edn/read-string (slurp "resources/edn/process-variables.edn")) key)]
    variables
    (throw (Exception. "No configured variables for process"))))
