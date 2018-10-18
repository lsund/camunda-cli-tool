(ns camunda-cli-tool.ui
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst]
            [camunda-cli-tool.external-task :as task]
            [camunda-cli-tool.decision-definition :as ddef]
            [clojure.string :as string]))

(def screen-width 120)

(def repl)

(defn print-menu-item [prefix & args]
  (printf "  %s %s\n" prefix (apply str args)))

(def root
  "Root node."
  {:title "Main Menu"
   :children {"pd" {:description "List process definitions" :next pdef/root}
              "pi" {:description "List active process instances" :next pinst/root}
              "et" {:description "List available external tasks" :next task/root}
              "dd" {:description "List Decision Definitions" :next ddef/root}}})

(defn show-title [title]
  (let [padding (apply str (take (quot (- screen-width (count title)) 2) (repeat \.)))]
    (str padding title padding)))

(defn print-node [{:keys [title children]}]
  (println (apply str (repeat screen-width "-")))
  (println (show-title title))
  (println)
  (if (not-empty children)
    (doseq [[x o] children]
      (cond
        (= :title x) nil
        (string? x) (printf "  (%s) %s\n" x (:description o))
        :default (throw (Exception. (str "Should not happen. Type: " (type x))))))
    (println " Nothing to display"))
  (println)
  (print-menu-item "(b) Back")
  (print-menu-item "(m) Main Menu")
  (print-menu-item "(q) Quit")
  (print-menu-item "(r) Refresh")
  (print "  \n  ? "))

(defn try-find-child-node [k children]
  "The first child in children for which k matches a substring of its :name."
  (some->> children
           (filter (fn [[_ child]]
                     (when-let [last-part (some-> child :description string/lower-case (string/split #"/") last)]
                       (re-find (re-pattern k) last-part))))
           first
           second))

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
  (let [next-node (get-in node [:children k :next])
        fun (get-in node [:children k :function])
        args (get-in node [:children k :args])]
    (cond
      next-node (repl (conj nodes (apply next-node args)))
      fun (let [result (apply fun args)]
            (println "Result: " (:value result))
            (if (:rebound result)
              (repl (next nodes))
              (repl nodes)))
      :default (if-let [{:keys [next args] :as child} (try-find-child-node k (:children node))]
                 (repl (conj nodes (apply next args)))
                 (do
                   (print-menu-item "Unknown command: " k)
                   (repl nodes))))))

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
    (print-node node)
    (flush)
    (let [k (read-line)]
      (println)
      (case k
        "q" (println "\n  Bye")
        "b" (backward-node k nodes)
        "m" (repl (list (last nodes)))
        "r" (if-let [key (:key node)]
              (loop-node key (next nodes))
              (repl nodes))
        (forward-node k node nodes)))))
