(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]
            [camunda-cli-tool.external-task :as task]
            [camunda-cli-tool.decision-definition :as ddef]
            [camunda-cli-tool.traverse :as traverse]
            [camunda-cli-tool.display :as display])
  (:gen-class))

(def root
  "Root node."
  {:title "Main Menu"
   :children {"pd" {:description "List process definitions" :next pdef/list-all}
              "pi" {:description "List active process instances" :next pinst/list-all}
              "et" {:description "List available external tasks" :next task/list-all}
              "dd" {:description "List Decision Definitions" :next ddef/list-all}}})

(defn -main [& args]
  (cond
    (empty? args) (display/print-node root)
    :default (traverse/find-node root args)))
