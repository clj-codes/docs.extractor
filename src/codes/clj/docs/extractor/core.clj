(ns codes.clj.docs.extractor.core
  (:require [codes.clj.docs.adapters :as adapters]
            [codes.clj.docs.analysis :as analysis])
  (:gen-class))

(defn extract!
  "Extract data from configured projects and generate Datalevin file."
  [_data]
  (println "extract!")
  ;; TODO create database
  (let [analysis-raw (analysis/extract!)]
    {:project (adapters/analysis->projects analysis-raw)
     :libraries (adapters/analysis->libraries analysis-raw)
     :definitions (adapters/analysis->definitions analysis-raw)}))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  (extract! {}))
