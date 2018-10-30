(ns camunda-cli-tool.http
  (:require [clj-http.client :as client]
            [camunda-cli-tool.config :as config]
            [slingshot.slingshot :refer [try+]]
            [camunda-api.convert-variables :as camunda-api]))

(def camunda-base-uri (:camunda-base-uri (config/load)))

(defn rest-url [resource]
  (str camunda-base-uri "/" resource))

(defn rest-get
  ([resource]
   (rest-get resource {}))
  ([resource params]
   (try+
    (client/get (rest-url resource) {:query-params params})
    (catch java.net.ConnectException _
      (println "Could not connect to: " camunda-base-uri)))))

(defn rest-post [resource variables]
  (client/post (rest-url resource)
               {:form-params {:variables (camunda-api/clj->engine variables)}
                :content-type :json
                :debug true}))

(defn rest-delete
  ([resource]
   (rest-delete resource {}))
  ([resource params]
   (client/delete (rest-url resource) {:query-params params})))
