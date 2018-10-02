(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst])
  (:gen-class))

(def screen-width 80)

(def repl)

(def root
  {:title "Main Menu"
   :children {"d" {:description "List Process Definitions" :next pdef/root}
              "i" {:description "List Active Process Instances" :next pinst/root}}})

(defn show-title [title]
  (let [padding (apply str (take (quot (- screen-width (count title)) 2) (repeat \.)))]
    (str padding title padding)))

(defn print-node [node]
  (println (show-title (:title node)))
  (doseq [[x o] (:children node)]
    (cond
      (= :title x) nil
      (string? x) (printf "(%s) %s\n" x (:description o))
      :default (throw (Exception. (str "Should not happen. Type: " (type x))))))
  (println "(b) Back")
  (println "(q) Quit"))

(defn forward-node [k node nodes]
  (let [child (get-in node [:children k :next])
        fun (get-in node [:children k :function])
        args (get-in node [:children k :args])]
    (if child
      (repl (conj nodes (apply child args))) ;
      (do
        (println "Result: " (apply fun args))
        (repl nodes)))))

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
    (let [k (read-line)]
      (case k
        "q" (println "Bye")
        "b" (backward-node nodes)
        (forward-node k node nodes)))))

(defn run []
  (println "Use the keys in (brackets) to navigate.")
  (repl (list root)))

(defn -main [& args]
  (run))
