(ns codes.clj.docs.extractor.core
  (:require [codes.clj.docs.extractor.adapters :as adapters]
            [codes.clj.docs.extractor.analysis :as analysis]
            [codes.clj.docs.extractor.config :as config]
            [codes.clj.docs.extractor.datalevin :as datalevin]
            [codes.clj.docs.extractor.log :refer [with-log]])
  (:gen-class))

(defn ^:private parse-projects [projects db-conn]
  (doseq [project projects]
    (println "parsing:" (key project))
    (let [[project-name project-config] project
          analysis-raw (with-log (analysis/extract-analysis! project-name project-config))
          datoms (with-log (adapters/analysis->datoms [analysis-raw]))]
      (datalevin/bulk-transact! datoms db-conn))))

(defn extract!
  "Extract data from configured projects and generate Datalevin file."
  [_data]
  (let [config (config/read! "resources/config.edn")
        db-conn (datalevin/open-db-conn config)]
    (with-log
      (parse-projects (:deps config) db-conn))))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  (extract! {}))
