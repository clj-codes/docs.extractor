(ns codes.clj.docs.adapters
  (:require [clojure.string :as str]))

(defn ^:private assoc-some-transient! [m k v]
  (if (nil? v) m (assoc! m k v)))

(defn ^:private assoc-some
  "Associates a key k, with a value v in a map m, if and only if v is not nil."
  ([m k v]
   (if (nil? v) m (assoc m k v)))
  ([m k v & kvs]
   (loop [acc (assoc-some-transient! (transient (or m {})) k v)
          kvs kvs]
     (if (next kvs)
       (recur (assoc-some-transient! acc (first kvs) (second kvs)) (nnext kvs))
       (if (zero? (count acc))
         m
         (persistent! acc))))))

(defn analysis->projects
  [analysis]
  (mapv
   (fn [{:keys [project]}]
     (let [[group artifact] (-> project :project-name (str/split #"/"))]
       (assoc-some {:project/name (:project-name project)}
                   :project/group group
                   :project/artifact artifact
                   :project/paths (mapv #(str/replace % (:deps/root project) "")
                                        (:paths project))
                   :project/url (:git/url project)
                   :project/tag (:git/tag project)
                   :project/sha (:git/sha project)
                   :project/manifest (:deps/manifest project))))
   analysis))

(defn analysis->libraries
  [analysis]
  (reduce
   (fn [accum {:keys [project libraries]}]
     (into accum
           (let [{:git/keys [url tag] :deps/keys [root]} project
                 [group artifact] (-> project :project-name (str/split #"/"))]
             (mapv (fn [{:keys [end-row meta name-end-col name-end-row name-row added
                                name author filename col name-col end-col doc row]}]
                     (let [trim-filename (str/replace filename root "")]
                       (assoc-some
                        {:library/project (:project-name project)
                         :library/group group
                         :library/artifact artifact
                         :library/name (str name)}
                        :library/end-row end-row
                        :library/meta meta
                        :library/name-end-col name-end-col
                        :library/name-end-row name-end-row
                        :library/name-row name-row
                        :library/added added
                        :library/author author
                        :library/filename trim-filename
                        :library/git-source (str url "/blob/" tag trim-filename "#L" row)
                        :library/col col
                        :library/name-col name-col
                        :library/end-col end-col
                        :library/doc doc
                        :library/row row)))
                   libraries))))
   []
   analysis))

(defn ^:private inrelevant-definitions [{:definition/keys [defined-by]}]
  (contains? #{"clojure.core/declare"} defined-by))

(defn analysis->definitions
  [analysis]
  (->> analysis
       (reduce
        (fn [accum {:keys [project definitions]}]
          (into accum
                (let [{:git/keys [url tag] :deps/keys [root]} project
                      [group artifact] (-> project :project-name (str/split #"/"))]
                  (mapv (fn [{:keys [fixed-arities end-row meta name-end-col
                                     name-end-row name-row added ns name author
                                     defined-by filename macro col name-col end-col
                                     arglist-strs varargs-min-arity doc row]}]
                          (let [trim-filename (str/replace filename root "")]
                            (assoc-some
                             {:definition/project (:project-name project)
                              :definition/group group
                              :definition/artifact artifact
                              :definition/name (str name)}
                             :definition/defined-by (some-> defined-by str)
                             :definition/library (some-> ns str)
                             :definition/fixed-arities fixed-arities
                             :definition/arglist-strs arglist-strs
                             :definition/end-row end-row
                             :definition/meta meta
                             :definition/name-end-col name-end-col
                             :definition/name-end-row name-end-row
                             :definition/name-row name-row
                             :definition/added added
                             :definition/author author
                             :definition/filename trim-filename
                             :definition/git-source (str url "/blob/" tag trim-filename "#L" row)
                             :definition/col col
                             :definition/name-col name-col
                             :definition/end-col end-col
                             :definition/doc doc
                             :definition/row row
                             :definition/macro macro
                             :definition/varargs-min-arity varargs-min-arity)))
                        definitions))))
        [])
       (remove inrelevant-definitions)))
