(ns camunda-cli-tool.ui
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]))

(def screen-width 80)

(def repl)

(def root
  {:title "Main Menu"
   :children {"pd" {:description "List Process Definitions" :next pdef/root}
              "pi" {:description "List Active Process Instances" :next pinst/root}}})

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
  (println "(m) Main Menu")
  (println "(q) Quit")
  (println "(r) Refresh"))

(defn forward-node [k node nodes]
  (let [child (get-in node [:children k :next])
        fun (get-in node [:children k :function])
        args (get-in node [:children k :args])]
    (if child
      (repl (conj nodes (apply child args))) ;
      (do
        (println "Result: " (apply fun args))
        (repl nodes)))))

(defn backward-node [k nodes]
  (if-let [previous (next nodes)]
    (repl previous)
    (repl nodes)))

(defn loop-node [k nodes]
  "Returns to the current node, after its content is re-loaded."
  (let [node (first nodes)
        child (get-in node [:children k :next])
        args (get-in node [:children k :args])]
    (let [x (conj nodes (apply child args))]
      (repl x))))

(defn repl [nodes]
  (let [node (first nodes)]
    (println (apply str (repeat 80 "-")))
    (print-node node)
    (flush)
    (let [k (read-line)]
      (case k
        "q" (println "Bye")
        "b" (backward-node k nodes)
        "m" (repl (list (last nodes)))
        "r" (if-let [key (:key node)]
              (loop-node key (next nodes))
              (repl nodes))
        (forward-node k node nodes)))))
