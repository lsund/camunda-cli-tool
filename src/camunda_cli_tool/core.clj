(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.ui :as ui])
  (:gen-class))

(defn -main [& args]
  (println "\n  Please use the keys displayed in (brackets) to navigate\n")
  (ui/repl (list ui/root)))
