(ns camunda-cli-tool.core
  (:require [camunda-cli-tool.process-definition :as pdef]
            [camunda-cli-tool.process-instance :as pinst])
  (:gen-class))

(def default-keymap
  {:title "Main Menu"
   \d {:description "List Process Definitions" :next (pdef/most-recent-keymap)}
   \i {:description "List Active Process Instances" :next (pinst/keymap)}})

(defn show-title [title]
  (let [padding (apply str (take (quot (- 80 (count title)) 2) (repeat \.)))]
    (str padding title padding)))

(defn print-keymap [keymap]
  (println (show-title (:title keymap)))
  (doseq [[x o] keymap]
    (cond
      (= :title x) nil
      (char? x) (printf "(%c) %s\n" x (:description o))
      (number? x) (printf "(%d) %s\n" x (:description o))
      :default (throw (Exception. (str "Should not happen. Type: " (type x))))))
  (println "(b) Back")
  (println "(q) Quit"))

(defn repl
  [keymaps]
  (let [keymap (first keymaps)]
    (println (apply str (repeat 80 "-")))
    (print-keymap keymap)
    (flush)
    (let [l (read-line)
          c (first l)]
      (case c
        \q (println "Bye")
        \b (if-let [previous (next keymaps)]
             (repl previous)
             (repl keymaps))
        (do
          (if-let [new-keymap (get-in keymap [c :next])]
            (repl (conj keymaps new-keymap))
            (let [fun (get-in keymap [c :function])
                  args (get-in keymap [c :args])]
              (println "Result: " (apply fun args))
              (repl keymaps))))))))

(defn run []
  (println "Use the keys in (brackets) to navigate.")
  (repl (list default-keymap)))

(defn -main [& args]
  (run))
