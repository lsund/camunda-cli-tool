(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.ui :as ui])
  (:gen-class))

(defn -main [& args]
  (println "Use the keys in (brackets) to navigate.")
  (ui/repl (list ui/root)))
