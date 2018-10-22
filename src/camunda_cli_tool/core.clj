(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.ui :as ui])
  (:gen-class))

(defn -main [& args]
  (cond
    (= (first args) "repl") (do
                              (println "\n  Please use the keys displayed in (brackets) to navigate\n")
                              (ui/repl (list ui/root)))
    (empty? args) (ui/print-node ui/root)
    :default (ui/find-node ui/root args)))
