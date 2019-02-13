(ns camunda-cli-tool.entity
  (:require [camunda-cli-tool.util :as util]))

(defn with-functions [description-fn handler-fn entity]
  (merge entity {:description (description-fn entity)
                 :handler-fn handler-fn
                 :handler-args [entity]}))

(defn all
  ([title description-fn handler-fn entities-fn]
   {:title title
    :children (util/build-indexed-map (partial with-functions description-fn handler-fn)
                                      (entities-fn))})
  ([title description-fn handler-fn entities-fn pred-fn]
   {:title title
    :children (util/build-indexed-map pred-fn
                                      (partial with-functions description-fn handler-fn)
                                      (entities-fn))}))
