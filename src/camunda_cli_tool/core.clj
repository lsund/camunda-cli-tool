(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst])
  (:gen-class))

(def root
  {:title "Main Menu"
   \d {:description "List Process Definitions" :next (pdef/root)}
   \i {:description "List Active Process Instances" :next (pinst/root)}})

(defn show-title [title]
  (let [padding (apply str (take (quot (- 80 (count title)) 2) (repeat \.)))]
    (str padding title padding)))

(defn print-node [node]
  (println (show-title (:title node)))
  (doseq [[x o] node]
    (cond
      (= :title x) nil
      (char? x) (printf "(%c) %s\n" x (:description o))
      (number? x) (printf "(%d) %s\n" x (:description o))
      :default (throw (Exception. (str "Should not happen. Type: " (type x))))))
  (println "(b) Back")
  (println "(q) Quit"))

(defn forward-node [c node nodes]
  (if-let [child (get-in node [c :next])]
    (repl (conj nodes child))
    (let [fun (get-in node [c :function])
          args (get-in node [c :args])]
      (println "Result: " (apply fun args))
      (repl nodes))))

(defn backward-node [nodes]
  (if-let [previous (next nodes)]
    (repl previous)
    (repl nodes)))

(defn repl
  [nodes]
  (let [node (first nodes)]
    (println (apply str (repeat 80 "-")))
    (print-node node)
    (flush)
    (let [l (read-line)
          c (first l)]
      (case c
        \q (println "Bye")
        \b (backward-node nodes)
        (forward-node c node nodes)))))

(defn run []
  (println "Use the keys in (brackets) to navigate.")
  (repl (list root)))

(defn -main [& args]
  (run))
