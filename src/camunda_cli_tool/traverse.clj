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

;; TODO rename args & args here
(defn find-node [node [arg & args]]
  "Takes a node and a list of string arguments."
  (let [next-node-fn (get-in node [:children arg :next])
        next-node-args (get-in node [:children arg :args])
        leaf-fn (get-in node [:children arg :function])]
    (cond
      (= arg "v") (leaf-fn (first next-node-args) (cli-args->map args))
      next-node-fn (let [next-node (apply next-node-fn next-node-args)]
                     (display/print-node next-node)
                     (if args
                       (find-node next-node args)
                       next-node))
      leaf-fn (let [result (apply leaf-fn next-node-args)]
            (println "Result: " (:value result))
            result)
      :default (if-let [child (try-find-child-node arg (:children node))]
                 (let [next-node-fn (:next child)]
                   (display/print-node (apply next-node-fn (:args child)))
                   (if args
                     (find-node (apply next-node-fn (:args child)) args)
                     (apply next-node-fn (:args child))))
                 (do
                   (display/print-menu-item "Unknown command: " arg)
                   (flush))))))

(defn forward-node [k node nodes]
  "Tries to advance down the action tree.
   If the current node contains a child for the given key `arg`, then read its node and continue.
   If the current node contains a function and arguments for the given key `arg`,
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
