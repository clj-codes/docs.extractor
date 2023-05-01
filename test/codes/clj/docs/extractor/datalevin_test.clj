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

        (is (match? {:project/manifest :pom
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
                     :project/group "org.clojure"}
                    (-> (d/q '[:find (pull ?e [*])
                               :in $ ?q
                               :where [?e :project/name ?q]]
                             db
                             "org.clojure/clojure")
                        ffirst)))

        (is (match? {:library/project {:db/id 1
                                       :project/manifest :pom
                                       :project/tag "clojure-1.11.1"
                                       :project/sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
                                       :project/url "https://github.com/clojure/clojure"
                                       :project/artifact "clojure"
                                       :project/paths ["/src/clj"
                                                       "/src/main/clojure"
                                                       "/src/main/java"
                                                       "/src/resources"]
                                       :project/name "org.clojure/clojure"
                                       :project/group "org.clojure"
                                       :project/id "org.clojure/clojure"}
                     :db/id 2
                     :library/artifact "clojure"
                     :library/name-end-col 19
                     :library/added "1.2"
                     :library/end-col 40
                     :library/end-row 39
                     :library/git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint.clj#L14"
                     :library/name-row 37
                     :library/meta {}
                     :library/row 14
                     :library/name-col 5
                     :library/author "Tom Faulhaber"
                     :library/col 1
                     :library/name "clojure.pprint"
                     :library/doc "A Pretty Printer for Clojure\n\nclojure.pprint implements a flexible system for printing structured data\nin a pleasing easy-to-understand format. Basic use of the pretty printer is \nsimple just call pprint instead of println. More advanced users can use \nthe building blocks provided to create custom output formats. \n\nOut of the box pprint supports a simple structured format for basic data \nand a specialized format for Clojure source code. More advanced formats \nincluding formats that don't look like Clojure data at all like XML and \nJSON can be rendered by creating custom dispatch functions. \n\nIn addition to the pprint function this module contains cl-format a text \nformatting function which is fully compatible with the format function in \nCommon Lisp. Because pretty printing directives are directly integrated with\ncl-format it supports very concise custom dispatch. It also provides\na more powerful alternative to Clojure's standard format function.\n\nSee documentation for pprint and cl-format for more information or \ncomplete documentation on the Clojure web site on GitHub."
                     :library/id "org.clojure/clojure/clojure.pprint"
                     :library/name-end-row 37
                     :library/filename "/src/clj/clojure/pprint.clj"
                     :library/group "org.clojure"}
                    (-> (d/q '[:find (pull ?e [* {:library/project [*]}])
                               :in $ ?q
                               :where [?e :library/name ?q]]
                             db
                             "clojure.pprint")
                        ffirst)))

        (d/close conn)))

    (util/delete-files dir)))
