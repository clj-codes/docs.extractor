(ns codes.clj.docs.extractor.analysis
  (:require [clj-kondo.core :as kondo]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.deps :as deps]))

(defn download-project!
  [project git]
  (-> (deps/resolve-deps
       {:deps {project git}
        :mvn/repos {"central" {:url "https://repo1.maven.org/maven2/"},
                    "clojars" {:url "https://repo.clojars.org/"}}}
       nil)
      (get project)
      (assoc :project-name (str project))))

(defn kondo-run!
  [paths]
  (-> {:lint paths
       :config {:output {:format :edn}
                :analysis {:arglists true
                           :var-definitions {:meta [:no-doc :skip-wiki :arglists]}
                           :namespace-definitions {:meta [:no-doc :skip-wiki]}}}}
      kondo/run!
      :analysis
      (dissoc :namespace-usages
              :var-usages)))

(defn extract-extras!
  [paths]
  (reduce (fn [accum path] (into accum (-> path io/resource slurp edn/read-string)))
          []
          paths))

(defn extract-analysis!
  [project-name project-config]
  (let [{:keys [paths] :as project-meta} (download-project! project-name project-config)
        {:keys [var-definitions namespace-definitions]} (kondo-run! paths)
        extra-definitions (extract-extras! (-> project-config :extras :definitions))]
    {:project project-meta
     :namespaces namespace-definitions
     :definitions (into var-definitions extra-definitions)}))

(defn extract!
  [config]
  (->> config
       :deps
       (mapv (fn [[project-name project-config]]
               (extract-analysis! project-name project-config)))))
