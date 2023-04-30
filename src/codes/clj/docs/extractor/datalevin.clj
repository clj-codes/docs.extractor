(ns codes.clj.docs.extractor.datalevin
  (:require [clojure.pprint :as pprint]
            [datalevin.core :as d]))

;; TODO: add id :db.unique/identity and ref :db.type/ref

(def project-schema
  {;:project/id     {:db/valueType :db.type/string :unique :db.unique/identity}
   :project/name     {:db/valueType :db.type/string
                      :db/fulltext  true}
   :project/group    {:db/valueType :db.type/string}
   :project/artifact {:db/valueType :db.type/string}
   :project/paths    {:db/valueType :db.type/string
                      :db/cardinality :db.cardinality/many}
   :project/url      {:db/valueType :db.type/string}
   :project/tag      {:db/valueType :db.type/string}
   :project/sha      {:db/valueType :db.type/string}
   :project/manifest {:db/valueType :db.type/keyword}})

(def library-schema
  {;:library/id           {:db/valueType :db.type/string :unique :db.unique/identity}
   :library/name         {:db/valueType :db.type/string
                          :db/fulltext  true}
   :library/project      {:db/valueType :db.type/string} ;todo change to ref
   ;:library/project      {:db/valueType :db.type/ref}
   :library/group        {:db/valueType :db.type/string}
   :library/artifact     {:db/valueType :db.type/string}
   :library/doc          {:db/valueType :db.type/string
                          :db/fulltext  true}
   :library/author       {:db/valueType :db.type/string}
   :library/filename     {:db/valueType :db.type/string}
   :library/git-source   {:db/valueType :db.type/string}
   :library/added        {:db/valueType :db.type/string}
   :library/row          {:db/valueType :db.type/long}
   :library/col          {:db/valueType :db.type/long}})

(def definition-schema
  {;:definition/id                {:db/valueType :db.type/string :unique :db.unique/identity}
   :definition/name              {:db/valueType :db.type/string
                                  :db/fulltext  true}
   :definition/library           {:db/valueType :db.type/string} ;todo change to ref
   ;:definition/library           {:db/valueType :db.type/ref}
   :definition/project           {:db/valueType :db.type/string} ;todo change to ref
   ;:definition/project           {:db/valueType :db.type/ref}
   :definition/group             {:db/valueType :db.type/string}
   :definition/artifact          {:db/valueType :db.type/string}
   :definition/doc               {:db/valueType :db.type/string
                                  :db/fulltext  true}
   :definition/filename          {:db/valueType :db.type/string}
   :definition/git-source        {:db/valueType :db.type/string}
   :definition/arglist-strs      {:db/valueType :db.type/string
                                  :db/cardinality :db.cardinality/many}
   :definition/varargs-min-arity {:db/valueType :db.type/long}
   :definition/added             {:db/valueType :db.type/string}
   :definition/macro             {:db/valueType :db.type/boolean}
   :definition/row               {:db/valueType :db.type/long}
   :definition/col               {:db/valueType :db.type/long}})

(def db-schemas
  (merge project-schema library-schema definition-schema))

(comment
  (require '[codes.clj.docs.extractor.fixtures.analysis :as fix])

  (let [conn (d/get-conn "/tmp/mydb" db-schemas)
        db (d/db conn)]
    (d/transact! conn (concat fix/projects-adapted
                              fix/libraries-adapted
                              fix/definitions-adapted))
    (pprint/pprint (d/q '[:find (pull ?e [*])
                          :in $ ?q
                          :where [?e :project/name ?q]]
                        db
                        "org.clojure/clojure"))
    (d/close conn)))
