(ns codes.clj.docs.extractor.analysis
  (:require [clj-kondo.core :as kondo]
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

(defn extract-analysis!
  [project git]
  (let [{:keys [paths] :as project-meta} (download-project! project git)
        {:keys [var-definitions namespace-definitions]} (kondo-run! paths)]
    {:project project-meta
     :libraries namespace-definitions
     :definitions var-definitions}))

(defn extract!
  [config]
  (->> config
       :deps
       (mapv (fn [[project git]]
               (extract-analysis! project git)))))
