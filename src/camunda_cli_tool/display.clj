(ns camunda-cli-tool.display)

(def screen-width 120)

(defn print-menu-item [prefix & args]
  (printf "  %s %s\n" prefix (apply str args)))

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
