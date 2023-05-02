(ns codes.clj.docs.extractor.adapters-test
  (:require [clojure.test :refer [deftest is testing]]
            [codes.clj.docs.extractor.adapters :as adapters]
            [codes.clj.docs.extractor.fixtures.analysis :as fixtures.analysis]
            [matcher-combinators.test :refer [match?]]))

(deftest analysis->projects-test
  (testing "analysis -> project"
    (is (match? fixtures.analysis/projects-adapted
                (adapters/analysis->projects fixtures.analysis/raw)))))

(deftest analysis->namespaces-test
  (testing "analysis -> namespaces"
    (is (match? fixtures.analysis/namespaces-adapted
                (adapters/analysis->namespaces fixtures.analysis/raw)))))

(deftest analysis->definitions-test
  (testing "analysis -> definitions"
    (is (match? fixtures.analysis/definitions-adapted
                (adapters/analysis->definitions fixtures.analysis/raw)))))

(deftest analysis->datoms-test
  (testing "analysis -> datoms"
    (is (= 4
           (count (adapters/analysis->datoms fixtures.analysis/raw))))))

(deftest id-by-test
  (testing "id-by should generate id string"
    (is (match? [{:name "juxt", :group "core", :row 1, :id "juxt/core/0"}
                 {:name "juxt", :group "core", :row 2, :id "juxt/core/1"}
                 {:name "assoc", :group "core", :row 1, :id "assoc/core/0"}]
                (adapters/id-by (juxt :name :group)
                                :id
                                [{:name "juxt" :group "core" :row 1}
                                 {:name "juxt" :group "core" :row 2}
                                 {:name "assoc" :group "core" :row 1}])))))
