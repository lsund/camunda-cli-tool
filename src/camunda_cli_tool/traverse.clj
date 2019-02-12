(ns camunda-cli-tool.traverse
  (:require [clojure.string :as string]
            [camunda-cli-tool.display :as display]))

(defn try-find-child-node [k children]
  "The first child in children for which k matches a substring of its :name."
  (some->> children
           (filter (fn [[_ child]]
                     (when-let [last-part (some-> child :description string/lower-case (string/split #"/") last)]
                       (re-find (re-pattern k) last-part))))
           first
           second))

(defn- cli-args->map [xs]
  (->> xs
      (map #(string/split % #":"))
      (map (fn [[x y]] [(keyword x) y]))
      (into {})))

(defn find-node [node [x & xs]]
  (let [next-node (get-in node [:children x :next])
        fun (get-in node [:children x :function])
        args (get-in node [:children x :args])]
    (cond
      (= x "cut") (fun (first args) (cli-args->map xs))
      next-node (do
                  (display/print-node (apply next-node args))
                  (if xs
                    (find-node (apply next-node args) xs)
                    (apply next-node args)))
      fun (let [result (apply fun args)]
            (println "Result: " (:value result))
            result)
      :default (if-let [{:keys [next args] :as child} (try-find-child-node x (:children node))]
                 (do
                   (display/print-node (apply next args))
                   (if xs
                     (find-node (apply next args) xs)
                     (apply next args)))
                 (do
                   (display/print-menu-item "Unknown command: " x)
                   (flush))))))

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
  (let [result (find-node node [k])]
    (cond
      (:rebound result) (next nodes)
      (:value result) nodes
      :default (conj nodes result))))

(defn backward-node [k nodes]
  (if-let [previous (next nodes)]
    previous
    nodes))

(defn loop-node [k nodes]
  "Returns to the current node, after its content is re-loaded."
  (let [node (first nodes)
        child (get-in node [:children k :next])
        args (get-in node [:children k :args])]
    (conj nodes (apply child args))))
