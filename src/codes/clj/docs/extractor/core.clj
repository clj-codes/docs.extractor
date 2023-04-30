(ns codes.clj.docs.extractor.core
  (:require [codes.clj.docs.extractor.adapters :as adapters]
            [codes.clj.docs.extractor.analysis :as analysis]
            [codes.clj.docs.extractor.config :as config]
            [codes.clj.docs.extractor.datalevin :as datalevin]
            [codes.clj.docs.extractor.log :refer [log->fn]])
  (:gen-class))

(defn extract!
  "Extract data from configured projects and generate Datalevin file."
  [_data]
  (let [config (config/read! "resources/config.edn")
        analysis-raw (log->fn (analysis/extract! config))
        datoms (adapters/analysis->datoms analysis-raw)]
    (log->fn (datalevin/bulk-transact! datoms config))))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  (extract! {}))
