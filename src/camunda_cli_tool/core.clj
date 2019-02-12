(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.ui :as ui]
            [camunda-cli-tool.traverse :as traverse])
  (:gen-class))

(defn -main [& args]
  (println args)
  (cond
    (= (first args) "repl") (do
                              (println "\n  Please use the keys displayed in (brackets) to navigate\n")
                              (ui/repl (list ui/root)))
    (empty? args) (ui/print-node ui/root)
    :default (traverse/find-node ui/root args)))
