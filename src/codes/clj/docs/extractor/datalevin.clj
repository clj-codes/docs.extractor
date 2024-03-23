(ns codes.clj.docs.extractor.datalevin
  (:require [datalevin.core :as d]
            [datalevin.interpret :refer [inter-fn]]
            [datalevin.search-utils :as su]))

;; TODO: add id :db.unique/identity and ref :db.type/ref

(def project-schema
  {:project/id       {:db/valueType :db.type/string
                      :unique :db.unique/identity}
   :project/name     {:db/valueType :db.type/string}
   :project/group    {:db/valueType :db.type/string
                      :db/fulltext  true
                      :db.fulltext/domains ["project"
                                            "project-group"]}
   :project/artifact {:db/valueType :db.type/string
                      :db/fulltext  true
                      :db.fulltext/domains ["project"
                                            "project-name"]}
   :project/paths    {:db/valueType :db.type/string
                      :db/cardinality :db.cardinality/many}
   :project/url      {:db/valueType :db.type/string}
   :project/tag      {:db/valueType :db.type/string}
   :project/sha      {:db/valueType :db.type/string}
   :project/manifest {:db/valueType :db.type/keyword}})

(def namespace-schema
  {:namespace/id           {:db/valueType :db.type/string
                            :unique :db.unique/identity}
   :namespace/name         {:db/valueType :db.type/string
                            :db/fulltext  true
                            :db.fulltext/domains ["namespace"
                                                  "namespace-name"]}
   :namespace/project      {:db/valueType :db.type/ref}
   :namespace/group        {:db/valueType :db.type/string}
   :namespace/artifact     {:db/valueType :db.type/string}
   :namespace/no-doc       {:db/valueType :db.type/boolean}
   :namespace/doc          {:db/valueType :db.type/string
                            :db/fulltext  true
                            :db.fulltext/autoDomain true
                            :db.fulltext/domains ["namespace"
                                                  "namespace-doc"]}
   :namespace/author       {:db/valueType :db.type/string}
   :namespace/filename     {:db/valueType :db.type/string}
   :namespace/git-source   {:db/valueType :db.type/string}
   :namespace/deprecated   {:db/valueType :db.type/string}
   :namespace/added        {:db/valueType :db.type/string}
   :namespace/row          {:db/valueType :db.type/long}
   :namespace/col          {:db/valueType :db.type/long}})

(def definition-schema
  {:definition/id                {:db/valueType :db.type/string
                                  :unique :db.unique/identity}
   :definition/name              {:db/valueType :db.type/string
                                  :db/fulltext  true
                                  :db.fulltext/domains ["definition"
                                                        "definition-name"]}
   :definition/namespace         {:db/valueType :db.type/ref}
   :definition/group             {:db/valueType :db.type/string}
   :definition/artifact          {:db/valueType :db.type/string}
   :definition/doc               {:db/valueType :db.type/string
                                  :db/fulltext  true
                                  :db.fulltext/domains ["definition"
                                                        "definition-doc"]}
   :definition/filename          {:db/valueType :db.type/string}
   :definition/git-source        {:db/valueType :db.type/string}
   :definition/arglist-strs      {:db/valueType :db.type/string
                                  :db/cardinality :db.cardinality/many}
   :definition/varargs-min-arity {:db/valueType :db.type/long}
   :definition/deprecated        {:db/valueType :db.type/string}
   :definition/added             {:db/valueType :db.type/string}
   :definition/macro             {:db/valueType :db.type/boolean}
   :definition/private           {:db/valueType :db.type/boolean}
   :definition/row               {:db/valueType :db.type/long}
   :definition/col               {:db/valueType :db.type/long}
   :definition/protocol-ns       {:db/valueType :db.type/string}
   :definition/protocol-name     {:db/valueType :db.type/string}})

(def db-schemas
  (merge project-schema namespace-schema definition-schema))

(defn merge-tokenizers
  "Merges the results of tokenizer a and b into one sequence."
  [tokenizer-a tokenizer-b]
  (inter-fn [^String s]
    (into (sequence (tokenizer-a s))
      (sequence (tokenizer-b s)))))

(def query-analyzer
  (su/create-analyzer
   {:tokenizer (merge-tokenizers
                (inter-fn [s] [[s 0 0]])
                (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
    :token-filters [su/lower-case-token-filter]}))

(def analyzer
  (su/create-analyzer
   {:tokenizer (merge-tokenizers
                (inter-fn [s] [[s 0 0]])
                (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
    :token-filters [su/lower-case-token-filter
                    su/prefix-token-filter]}))

(defn open-db-coon [config]
  (-> config :db :dir
      (d/get-conn db-schemas
                  {:search-domains {"project-name" {:query-analyzer query-analyzer
                                                    :analyzer analyzer}
                                    "namespace-name" {:query-analyzer query-analyzer
                                                      :analyzer analyzer}
                                    "definition-name" {:query-analyzer query-analyzer
                                                       :analyzer analyzer}}})))

(defn close-db-conn [db-conn]
  #_(d/close db-conn))

(defn bulk-transact! [datoms conn]
  (d/transact! conn datoms))
