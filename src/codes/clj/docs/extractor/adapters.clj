(ns codes.clj.docs.extractor.adapters
  (:require [clojure.string :as str]))

(defn ^:private assoc-some-transient!
  [m k v]
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

(defn ^:private group-multi-langs
  [items]
  (->> items
       (group-by
        (juxt :ns :name :row))
       (reduce
        (fn [acc2 [_k v]]
          (merge acc2
                 (reduce
                  (fn [acc cur]
                    (let [langs (->> (into (or [(:lang acc)] []) [(:lang cur)])
                                     (remove nil?)
                                     flatten
                                     vec)]
                      (-> (merge acc cur)
                          (assoc :lang langs))))
                  {}
                  v)))
        [])))

(defn id-by
  [f index-key coll]
  (->> coll
       (group-by f)
       (reduce
        (fn [acc [grouped-keys values]]
          (into acc (map-indexed
                     (fn [index value]
                       (assoc value
                              index-key (str/join "/" (conj grouped-keys index))))
                     values)))
        [])))

(defn analysis->projects
  [analysis]
  (mapv
   (fn [{:keys [project]}]
     (let [[group artifact] (-> project :project-name (str/split #"/"))]
       (assoc-some {:project/id (:project-name project)
                    :project/name (:project-name project)}
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
             (->> libraries
                  group-multi-langs
                  (mapv (fn [{:keys [end-row meta name-end-col name-end-row name-row added
                                     name author filename col name-col end-col doc row]}]
                          (let [trim-filename (str/replace filename root "")]
                            (assoc-some
                             {:library/id (str/join "/" [group artifact name])
                              :library/project {:project/id (:project-name project)}
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
                             :library/row row))))))))
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
                  (->> definitions
                       group-multi-langs
                       (mapv (fn [{:keys [fixed-arities end-row meta name-end-col
                                          name-end-row name-row added ns name author
                                          defined-by filename macro col name-col end-col
                                          arglist-strs varargs-min-arity doc row
                                          private protocol-ns protocol-name]}]
                               (let [trim-filename (str/replace filename root "")]
                                 (assoc-some
                                  {:definition/group group
                                   :definition/artifact artifact
                                   :definition/name (str name)}
                                  :definition/defined-by (some-> defined-by str)
                                  :definition/namespace (some-> ns str)
                                  :definition/library (when ns {:library/id (str/join "/" [group artifact ns])})
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
                                  :definition/macro (boolean macro)
                                  :definition/varargs-min-arity varargs-min-arity
                                  :definition/private (boolean private)
                                  :definition/protocol-ns (some-> protocol-ns str)
                                  :definition/protocol-name (some-> protocol-name str)))))))))
        [])
       (id-by (juxt :definition/group
                    :definition/artifact
                    :definition/namespace
                    :definition/name)
              :definition/id)
       (remove inrelevant-definitions)))

(defn analysis->datoms
  [analysis]
  (concat (analysis->projects analysis)
          (analysis->libraries analysis)
          (analysis->definitions analysis)))
