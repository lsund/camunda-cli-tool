(ns camunda-cli-tool.http
  (:require [clj-http.client :as client]
            [slingshot.slingshot :refer [try+]]))

(def camunda-base-uri "http://camunda.dev.lambdawerk.com:8080/engine-rest")

(defn rest-url [resource]
  (str camunda-base-uri "/" resource))

(defn rest-get [resource]
  (try+
   (client/get (rest-url resource))
   (catch java.net.ConnectException _
     (println "Could not connect to: " camunda-base-uri))))

(defn rest-post [resource variables]
  (client/post (rest-url resource)
               {:form-params {:variables variables}
                :content-type :json}))

(defn rest-delete [resource]
  (client/delete (rest-url resource)))
