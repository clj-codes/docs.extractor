(ns codes.clj.docs.extractor.datalevin-test
  (:require [clojure.test :refer [deftest is testing]]
            [codes.clj.docs.extractor.adapters :as adapters]
            [codes.clj.docs.extractor.datalevin :as datalevin]
            [codes.clj.docs.extractor.fixtures.analysis :as fixtures.analysis]
            [datalevin.core :as d]
            [datalevin.util :as util]
            [matcher-combinators.test :refer [match?]])
  (:import [java.util UUID]))

(deftest analysis->datoms-test
  (let [dir (util/tmp-dir (str "lmdb-test-" (UUID/randomUUID)))]
    (datalevin/bulk-transact!
     (adapters/analysis->datoms fixtures.analysis/raw)
     {:db {:dir dir}})

    (testing "check data exists in database"
      (let [conn (d/get-conn dir datalevin/db-schemas)
            db (d/db conn)]

        (is (= 4
               (-> (d/q '[:find (count ?e)
                          :where [?e]]
                        db)
                   ffirst)))

        (is (match? [{:project/manifest :pom
                      :project/tag "clojure-1.11.1"
                      :project/sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
                      :project/url "https://github.com/clojure/clojure"
                      :project/artifact "clojure"
                      :project/paths ["/src/clj"
                                      "/src/main/clojure"
                                      "/src/main/java"
                                      "/src/resources"]
                      :db/id 1
                      :project/name "org.clojure/clojure"
                      :project/group "org.clojure"}]
                    (-> (d/q '[:find (pull ?e [*])
                               :in $ ?q
                               :where [?e :project/name ?q]]
                             db
                             "org.clojure/clojure")
                        first)))

        (d/close conn)))

    (util/delete-files dir)))

(comment
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
  ; tests with temporary database
  (let [db (-> (d/empty-db "/tmp/mydb"
                           {:text {:db/valueType :db.type/string
                                   :db/fulltext  true}})
               (d/db-with
                [{:db/id 1 :text "assoc!"}
                 {:db/id 2 :text "assoc"}
                 {:db/id 3 :text "assoc-in"}
                 {:db/id 4 :text "assoc-dom"}
                 {:db/id 5 :text "assoc-meta"}
                 {:db/id 6 :text "associative?"}]))]
    (d/q '[:find (pull ?e [*])
           :in $ ?q
           :where [(fulltext $ ?q) [[?e ?a ?v]]]]
         db
         "assoc")))
