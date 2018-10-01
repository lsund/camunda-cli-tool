(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]))

(defn make-action [c]
  (case c
    \d (pdef/show-unique-process-definitions)
    \i (pinst/show-process-instances)
    :default))

(defn repl
  [m]
  (println (apply str (repeat 80 "-")))
  (println "(d) List Process Definitions")
  (println "(i) List Active Process Instances")
  (print m)
  (let [l (read-line)
        c (first l)]
    (if (= c \q)
      (println "Bye")
      (do
        (println)
        (make-action c)
        (repl m)))))

(defn run []
  (print "Use 'q' for quit")
  (repl "? "))
