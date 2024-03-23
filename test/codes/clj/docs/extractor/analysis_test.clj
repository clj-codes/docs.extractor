(ns codes.clj.docs.extractor.analysis-test
  (:require [clojure.test :refer [deftest is testing]]
            [codes.clj.docs.extractor.analysis :as analysis]
            [codes.clj.docs.extractor.fixtures.analysis :as fixtures.analysis]
            [matcher-combinators.test :refer [match?]]))

(deftest build-paths-test
  (testing "checking fn io"
    (is (match? ["/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/main/java"
                 "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/main/clojure"
                 "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/resources"
                 "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/clj"
                 "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/modules/repo/src"
                 "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/modules/repo-core/src"
                 "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/modules/repo-dev/src"]
                (analysis/build-paths (-> fixtures.analysis/raw first :project)
                                      ["modules/repo/src"
                                       "modules/repo-core/src"
                                       "modules/repo-dev/src"])))))

(deftest resolved-deps->project-meta-test
  (testing "checking fn io"
    (is (match? {:project-name "project-name"
                 :group "group-name"
                 :git/url "https://github.com/clojure/clojure"
                 :git/tag "clojure-1.11.1"
                 :paths ["/Users/username/.gitlibs/libs/org.clojure/clojure/60ef6/src/main/java"
                         "/Users/username/.gitlibs/libs/org.clojure/clojure/60ef6/src/main/clojure"]
                 :git/sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
                 :deps/manifest :pom
                 :parents #{[]}
                 :artifact "artifact"
                 :deps/root "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6"}
                (analysis/resolved-deps->project-meta (-> fixtures.analysis/raw first :project)
                                                      "project-name"
                                                      "artifact"
                                                      "group-name"
                                                      ["/Users/username/.gitlibs/libs/org.clojure/clojure/60ef6/src/main/java"
                                                       "/Users/username/.gitlibs/libs/org.clojure/clojure/60ef6/src/main/clojure"])))))
