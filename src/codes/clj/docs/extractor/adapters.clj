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
     (let [{:keys [group artifact]} project]
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

(defn analysis->namespaces
  [analysis]
  (reduce
   (fn [accum {:keys [project namespaces]}]
     (into accum
           (let [{:keys [group artifact] :git/keys [url tag sha] :deps/keys [root]} project]
             (->> namespaces
                  group-multi-langs
                  (mapv (fn [{:keys [end-row meta name-end-col name-end-row name-row deprecated added
                                     name author filename col name-col end-col doc no-doc row lang]}]
                          (let [trim-filename (str/replace filename root "")]
                            (assoc-some
                             {:namespace/id (str/join "/" [group artifact name])
                              :namespace/project {:project/id (:project-name project)}
                              :namespace/group group
                              :namespace/artifact artifact
                              :namespace/name (str name)}
                             :namespace/end-row end-row
                             :namespace/meta meta
                             :namespace/name-end-col name-end-col
                             :namespace/name-end-row name-end-row
                             :namespace/name-row name-row
                             :namespace/deprecated (some-> deprecated str)
                             :namespace/added added
                             :namespace/author author
                             :namespace/filename trim-filename
                             :namespace/git-source (str url "/blob/" (or tag sha) trim-filename "#L" row)
                             :namespace/col col
                             :namespace/name-col name-col
                             :namespace/end-col end-col
                             :namespace/doc doc
                             :namespace/no-doc (some-> no-doc boolean)
                             :namespace/row row
                             :namespace/lang lang))))))))
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
                (let [{:keys [group artifact] :git/keys [url tag sha] :deps/keys [root]} project]
                  (->> definitions
                       group-multi-langs
                       (mapv (fn [{:keys [fixed-arities end-row meta name-end-col
                                          name-end-row name-row added deprecated ns name author
                                          defined-by filename macro col name-col end-col
                                          arglist-strs varargs-min-arity doc row
                                          private protocol-ns protocol-name lang]}]
                               (let [trim-filename (when filename (str/replace filename root ""))]
                                 (assoc-some
                                  {:definition/group group
                                   :definition/artifact artifact
                                   :definition/name (str name)}
                                  :definition/defined-by (some-> defined-by str)
                                  :definition/namespace (when ns
                                                          {:namespace/id (str/join "/" [group artifact ns])
                                                           :namespace/name (str ns)})
                                  :definition/fixed-arities fixed-arities
                                  :definition/arglist-strs arglist-strs
                                  :definition/end-row end-row
                                  :definition/meta meta
                                  :definition/name-end-col name-end-col
                                  :definition/name-end-row name-end-row
                                  :definition/name-row name-row
                                  :definition/added added
                                  :definition/deprecated (some-> deprecated str)
                                  :definition/author author
                                  :definition/filename trim-filename
                                  :definition/git-source (when trim-filename
                                                           (str url "/blob/" (or tag sha) trim-filename "#L" row))
                                  :definition/col col
                                  :definition/name-col name-col
                                  :definition/end-col end-col
                                  :definition/doc doc
                                  :definition/row row
                                  :definition/macro (some-> macro boolean)
                                  :definition/varargs-min-arity varargs-min-arity
                                  :definition/private (boolean private)
                                  :definition/protocol-ns (some-> protocol-ns str)
                                  :definition/protocol-name (some-> protocol-name str)
                                  :definition/lang lang))))))))
        [])
       (id-by (juxt :definition/group
                    :definition/artifact
                    #(get-in % [:definition/namespace :namespace/name])
                    :definition/name)
              :definition/id)
       (remove inrelevant-definitions)))

(defn analysis->datoms
  [analysis]
  (concat (analysis->projects analysis)
          (analysis->namespaces analysis)
          (analysis->definitions analysis)))
