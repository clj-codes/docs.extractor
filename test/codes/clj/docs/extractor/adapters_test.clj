(ns codes.clj.docs.extractor.adapters-test
  (:require [clojure.test :refer [deftest is testing]]
            [codes.clj.docs.extractor.adapters :as adapters]
            [codes.clj.docs.extractor.fixtures.analysis :as fixtures.analysis]
            [matcher-combinators.test :refer [match?]]))

(deftest analysis->projects-test
  (testing "analysis -> project"
    (is (match? fixtures.analysis/projects-adapted
                (adapters/analysis->projects fixtures.analysis/raw)))))

(deftest analysis->libraries-test
  (testing "analysis -> libraries"
    (is (match? fixtures.analysis/libraries-adapted
                (adapters/analysis->libraries fixtures.analysis/raw)))))

(deftest analysis->definitions-test
  (testing "analysis -> definitions"
    (is (match? fixtures.analysis/definitions-adapted
                (adapters/analysis->definitions fixtures.analysis/raw)))))

(deftest analysis->datoms-test
  (testing "analysis -> datoms"
    (is (= 4
           (count (adapters/analysis->datoms fixtures.analysis/raw))))))
