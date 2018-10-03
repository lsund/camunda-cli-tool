(ns camunda-cli-tool.ui
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]))

(def screen-width 120)

(def repl)

(def root
  {:title "Main Menu"
   :children {"pd" {:description "List Process Definitions" :next pdef/root}
              "pi" {:description "List Active Process Instances" :next pinst/root}}})

(defn show-title [title]
  (let [padding (apply str (take (quot (- screen-width (count title)) 2) (repeat \.)))]
    (str padding title padding)))

(defn print-node [{:keys [title children]}]
  (println (show-title title))
  (println)
  (if (not-empty children)
    (doseq [[x o] children]
      (cond
        (= :title x) nil
        (string? x) (printf "(%s) %s\n" x (:description o))
        :default (throw (Exception. (str "Should not happen. Type: " (type x))))))
    (println "Nothing to display"))
  (println)
  (println "(b) Back")
  (println "(m) Main Menu")
  (println "(q) Quit")
  (println "(r) Refresh"))

(defn forward-node [k node nodes]
  "Tries to advance down the action tree.
   If the current node contains a child for the given key k, then read its node and continue.
   If the current node contains a function and arguments for the given key k,
   then this is a leaf-node and the function is called with the arguments. The function should
   return a map containing the :value and :rebound keys. :value specified the return value of
   the function call and :rebound specifies weather to return to the previous menu in the
   application or not.
   For example, when invoking the function stop-process!, the process is deleted from the
   application. Then, the correct action is to return to the previous menu that lists all
   current processes.
   "
  (let [child (get-in node [:children k :next])
        fun (get-in node [:children k :function])
        args (get-in node [:children k :args])]
    (println)
    (cond
      child (repl (conj nodes (apply child args)))
      fun (let [result (apply fun args)]
            (println "Result: " (:value result))
            (if (:rebound result)
              (repl (next nodes))
              (repl nodes)))
      :default (do (println "Unknown command: " k)
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
    (println (apply str (repeat screen-width "-")))
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
