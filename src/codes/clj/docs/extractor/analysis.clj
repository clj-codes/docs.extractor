(ns codes.clj.docs.extractor.analysis
  (:require [clj-kondo.core :as kondo]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.deps :as deps]))

(def ^:private mvn-repos
  {"central" {:url "https://repo1.maven.org/maven2/"}
   "clojars" {:url "https://repo.clojars.org/"}})

(defn build-paths [resolved-deps source-paths]
  (if source-paths
    (into (:paths resolved-deps)
          (map #(str (:deps/root resolved-deps) "/" %) source-paths))
    (:paths resolved-deps)))

(defn resolved-deps->project-meta
  [resolved-deps project-name artifact group paths]
  (assoc resolved-deps
         :project-name project-name
         :artifact artifact
         :group group
         :paths paths))

(defn download-project!
  [project git]
  (let [project-name (str project)
        [group artifact] (str/split project-name #"/")
        group-name (or (:project/group git) group)
        resolved-deps (-> (deps/resolve-deps
                           {:deps {project git}
                            :mvn/repos mvn-repos}
                           nil)
                          (get project))
        paths (build-paths resolved-deps (:project/source-paths git))]
    (resolved-deps->project-meta resolved-deps
                                 project-name
                                 artifact
                                 group-name
                                 paths)))

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
