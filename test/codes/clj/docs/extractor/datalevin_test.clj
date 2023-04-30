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
