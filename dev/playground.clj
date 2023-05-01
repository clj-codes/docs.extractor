(ns dev.playground
  (:require [codes.clj.docs.extractor.core :as core]
            [codes.clj.docs.extractor.datalevin :as datalevin]
            [datalevin.core :as d]
            [datalevin.util :as util]))

(comment
  ; reset database
  (let [dir "target/docs-db"]
    (println "deleting")
    (try (util/delete-files dir) (catch Exception _))
    (println "bulking")
    (core/extract! {}))

  ; tests with generated database
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find (pull ?e [*]) ?a ?v
                             :in $ ?q
                             :where
                             [(fulltext $ ?q) [[?e ?a ?v]]]]
                           db
                           "assoc"))]
    (d/close conn)
    result)

  ; regex searching
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (->> (d/q '[:find [(pull ?e [:definition/id
                                                   :definition/name
                                                   :definition/group
                                                   :definition/artifact
                                                   :definition/namespace]) ...]
                                  :in $ ?q
                                  :where
                                  [(str ".*" ?q ".*") ?pattern]
                                  [(re-pattern ?pattern) ?regex]
                                  [(re-matches ?regex ?name)]
                                  [?e :definition/name ?name]
                                  [?e :definition/private false]
                                  (not [?e :definition/defined-by "cljs.core/defprotocol"])]
                                db
                                "assoc")
                           (sort-by (juxt
                                     :definition/id
                                     :definition/name))))]
    (d/close conn)
    result)

  ; count all data
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find (count ?e)
                             :in $ ?q
                             :where [?e]]
                           db
                           "assoc"))]
    (d/close conn)
    result)

  ; tests with temporary database
  (let [db (-> (d/empty-db "/tmp/mydb"
                           {:text {:db/valueType :db.type/string}})
               (d/db-with
                [{:db/id 1 :text "assoc!"}
                 {:db/id 2 :text "assoc"}
                 {:db/id 3 :text "assoc-in"}
                 {:db/id 4 :text "assoc-dom"}
                 {:db/id 5 :text "assoc-meta"}
                 {:db/id 6 :text "associative?"}]))]
    (d/q '[:find (pull ?e [*])
           :in $ ?q
           :where ;[(fulltext $ ?q) [[?e ?a ?v]]]
           [(str ".*" ?q ".*") ?pattern]
           [(re-pattern ?pattern) ?regex]
           [(re-matches ?regex ?name)]
           [?e :text ?name]]
         db
         "assoc")))
