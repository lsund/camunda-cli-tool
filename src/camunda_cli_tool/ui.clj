(ns camunda-cli-tool.ui
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]
            [camunda-cli-tool.external-task :as task]
            [camunda-cli-tool.decision-definition :as ddef]
            [camunda-cli-tool.display :as display]
            [camunda-cli-tool.traverse :as traverse]
            [clojure.string :as string]))

(def root
  "Root node."
  {:title "Main Menu"
   :children {"pd" {:description "List process definitions" :next pdef/root}
              "pi" {:description "List active process instances" :next pinst/root}
              "et" {:description "List available external tasks" :next task/root}
              "dd" {:description "List Decision Definitions" :next ddef/root}}})

(defn repl [nodes]
  (let [node (first nodes)]
    (display/print-node node)
    (flush)
    (let [k (read-line)]
      (println)
      (case k
        "q" (println "\n  Bye")
        "b" (repl (traverse/backward-node k nodes))
        "m" (repl (list (last nodes)))
        "r" (if-let [key (:key node)]
              (repl (traverse/loop-node key (next nodes)))
              (repl nodes))
        (repl (traverse/forward-node k node nodes))))))
