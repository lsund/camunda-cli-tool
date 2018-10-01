(ns camunda-cli-tool.http
  (:require [clj-http.client :as client]))

(def camunda-base-uri "http://localhost:8080/engine-rest")

(defn rest-url [resource]
  (str camunda-base-uri "/" resource))

(defn make-rest-call [resource]
  (client/get (rest-url resource)))
