(ns camunda-cli-tool.config
  (:require [clojure.edn :as edn]))

(def config-file "resources/edn/config.edn")

(defn load []
  (-> config-file slurp edn/read-string))

(defn process-variables [key]
  (if-let [variables (get-in (edn/read-string (slurp "resources/edn/process-variables.edn"))
                             [key :default])]
    variables
    (throw (Exception. (str "No configured variables for process.\n"
                            "Default variables need to be cofigured in file: \n"
                            config-file "\n"
                            "See https://github.com/lsund/camunda-cli-tool for more information.")))))

(defn required-keys [key]
  (if-let [keys (get-in (edn/read-string (slurp "resources/edn/process-variables.edn"))
                        [key :required])]
    keys
    (throw (Exception. "No configured required keys for process"))))
