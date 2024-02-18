(ns codes.clj.docs.extractor.datalevin
  (:require [datalevin.core :as d]))

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

(defn bulk-transact! [datoms config]
  (let [conn (-> config :db :dir (d/get-conn db-schemas))]
    (d/transact! conn datoms)
    (d/close conn)))
