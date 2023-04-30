(ns codes.clj.docs.extractor.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn read! [config-file]
  (->> config-file
       io/file
       slurp
       edn/read-string))
