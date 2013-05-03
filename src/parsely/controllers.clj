(ns parsely.controllers
  (:use [slingshot.slingshot :only [throw+]]
        [clojure-commons.error-codes])
  (:require [cheshire.core :as json]
            [hoot.rdf :as rdf]
            [parsely.actions :as actions]))

(defn check-missing-params
  [params required-keys]
  (let [not-valid? #(not (contains? params %))]
    (if (some not-valid? required-keys)
    (throw+ {:error_code ERR_BAD_OR_MISSING_FIELD
             :fields (filter not-valid? required-keys)}))))

(defn check-params-valid
  [params func-map]
  (let [not-valid? #(not ((last %1) (get params (first %1))))
        field-seq  (seq func-map)]
    (when (some not-valid? field-seq)
      (throw+ {:error_code ERR_BAD_OR_MISSING_FIELD
               :fields     (mapv first (filter not-valid? field-seq))}))))

(defn validate-params
  [params func-map]
  (check-missing-params params (keys func-map))
  (check-params-valid params func-map))

(defn parse
  [body params]
  (validate-params body {:url string?})
  (json/generate-string (actions/parse (:user params) (:url body))))

(defn classes
  [params]
  (validate-params params {:url string?})
  (json/generate-string (actions/classes (:url params))))

(defn properties
  [params]
  (validate-params params {:url string? :class string?})
  (json/generate-string 
    (actions/properties (:url params) (:class params))))

(defn triples
  [params]
  (validate-params params {:url string? :type #(contains? (set rdf/accepted-languages) %)})
  (json/generate-string
    (actions/triples (:url params) (:type params))))