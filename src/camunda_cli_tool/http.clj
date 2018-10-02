(ns camunda-cli-tool.http
  (:require [clj-http.client :as client]))

(def camunda-base-uri "http://localhost:8080/engine-rest")

(defn rest-url [resource]
  (str camunda-base-uri "/" resource))

(defn rest-get [resource]
  (client/get (rest-url resource)))

(defn rest-post [resource variables]
  (client/post (rest-url resource)
               {:form-params {:variables variables}
                :content-type :json}))
