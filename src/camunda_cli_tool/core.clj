(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]))

(def default-keymap
  {\d {:description "List Process Definitions" :next (pdef/most-recent-keymap)}
   \i {:description "List Active Process Instances" :next (pinst/keymap)}
   \q {:description "Quit" :next nil}})

(defn show-keymap [keymap]
  (doseq [[x o] keymap]
    (cond
        (char? x) (printf "(%c) %s\n" x (:description o))
        (number? x) (printf "(%d) %s\n" x (:description o))
        :default (throw (Exception. (str "Unknown type: " (type x)))))))

(defn repl
  [keymap]
  (println (apply str (repeat 80 "-")))
  (show-keymap keymap)
  (flush)
  (let [l (read-line)
        c (first l)]
    (if (= c \q)
      (println "Bye")
      (do
        (if-let [new-keymap (get-in keymap [c :next])]
          (repl new-keymap)
          (let [fun (get-in keymap [c :function])
                args (get-in keymap [c :args])]
            (println "Result: " (apply fun args))
            (repl keymap)))))))

(defn run []
  (println "Use 'q' for quit")
  (repl default-keymap))

(defn -main [& args]
  (run))
