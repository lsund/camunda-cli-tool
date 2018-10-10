(defproject camunda-cli-tool "0.3.3-SNAPSHOT"
  :description "A tool for quickly interacting with camunda through the command line."
  :url "https://github.com/lsund/camunda-cli-tool"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [medley "1.0.0"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]
                 [camel-snake-kebab "0.4.0"]
                 [slingshot "0.12.2"]]
  :main camunda-cli-tool.core
  :aot [camunda-cli-tool.core])
