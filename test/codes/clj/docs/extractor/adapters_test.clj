(ns codes.clj.docs.extractor.adapters-test
  (:require [clojure.test :refer [deftest is testing]]
            [codes.clj.docs.extractor.adapters :as adapters]
            [matcher-combinators.test :refer [match?]]))

(def analysis-fixture
  [{:project {:git/url "https://github.com/clojure/clojure"
              :git/tag "clojure-1.11.1"
              :git/sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
              :deps/manifest :pom
              :deps/root "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6"
              :parents #{[]}
              :paths ["/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/main/java"
                      "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/main/clojure"
                      "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/resources"
                      "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/clj"]
              :project-name "org.clojure/clojure"}
    :libraries [{:end-row 39
                 :meta {}
                 :name-end-col 19
                 :name-end-row 37
                 :name-row 37
                 :added "1.2"
                 :name 'clojure.pprint
                 :author "Tom Faulhaber"
                 :filename "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/clj/clojure/pprint.clj"
                 :col 1
                 :name-col 5
                 :end-col 40
                 :doc "A Pretty Printer for Clojure\n\nclojure.pprint implements a flexible system for printing structured data\nin a pleasing easy-to-understand format. Basic use of the pretty printer is \nsimple just call pprint instead of println. More advanced users can use \nthe building blocks provided to create custom output formats. \n\nOut of the box pprint supports a simple structured format for basic data \nand a specialized format for Clojure source code. More advanced formats \nincluding formats that don't look like Clojure data at all like XML and \nJSON can be rendered by creating custom dispatch functions. \n\nIn addition to the pprint function this module contains cl-format a text \nformatting function which is fully compatible with the format function in \nCommon Lisp. Because pretty printing directives are directly integrated with\ncl-format it supports very concise custom dispatch. It also provides\na more powerful alternative to Clojure's standard format function.\n\nSee documentation for pprint and cl-format for more information or \ncomplete documentation on the Clojure web site on GitHub."
                 :row 14}]
    :definitions [{:end-row 109
                   :meta {}
                   :name-end-col 30
                   :name-end-row 109
                   :name-row 109
                   :ns 'clojure.pprint
                   :name 'format-simple-number
                   :defined-by 'clojure.core/declare
                   :filename "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/clj/clojure/pprint/pprint_base.clj"
                   :col 1
                   :name-col 10
                   :end-col 31
                   :row 109}
                  {:end-row 327
                   :meta {:arglists '[[options* body]]}
                   :name-end-col 31
                   :name-end-row 302
                   :name-row 302
                   :added "1.2"
                   :ns 'clojure.pprint
                   :name 'pprint-logical-block
                   :defined-by 'clojure.core/defmacro
                   :filename "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/clj/clojure/pprint/pprint_base.clj"
                   :macro true
                   :col 1
                   :name-col 11
                   :end-col 16
                   :arglist-strs ["[& args]"]
                   :varargs-min-arity 0
                   :doc
                   "Execute the body as a pretty printing logical block with output to *out* which \nmust be a pretty printing writer. When used from pprint or cl-format this can be \nassumed. \n\nThis function is intended for use when writing custom dispatch functions.\n\nBefore the body the caller can optionally specify options: :prefix :per-line-prefix \nand :suffix."
                   :row 302}
                  {:fixed-arities #{1 2}
                   :end-row 35
                   :meta {}
                   :name-end-col 18
                   :name-end-row 11
                   :name-row 11
                   :added "1.3"
                   :ns 'clojure.pprint
                   :name 'print-table
                   :defined-by 'clojure.core/defn
                   :filename "/Users/username/.gitlibs/libs/org.clojure/clojure/ce55092f2b2f5481d25cff6205470c1335760ef6/src/clj/clojure/pprint/print_table.clj"
                   :col 1
                   :name-col 7
                   :end-col 51
                   :arglist-strs ["[ks rows]" "[rows]"]
                   :doc "Prints a collection of maps in a textual table. Prints table headings\n   ks and then a line of output for each row corresponding to the keys\n   in ks. If ks are not specified use the keys of the first item in rows."
                   :row 11}]}])

(deftest analysis->projects-test
  (testing "analysis -> project"
    (is (match? [#:project{:name "org.clojure/clojure"
                           :group "org.clojure"
                           :artifact "clojure"
                           :paths ["/src/main/java"
                                   "/src/main/clojure"
                                   "/src/resources"
                                   "/src/clj"]
                           :url "https://github.com/clojure/clojure"
                           :tag "clojure-1.11.1"
                           :sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
                           :manifest :pom}]
                (adapters/analysis->projects analysis-fixture)))))

(deftest analysis->libraries-test
  (testing "analysis -> libraries"
    (is (match? [#:library{:project "org.clojure/clojure"
                           :artifact "clojure"
                           :name-end-col 19
                           :added "1.2"
                           :group "org.clojure"
                           :end-col 40
                           :end-row 39
                           :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint.clj#L14"
                           :name-row 37
                           :meta {}
                           :row 14
                           :name-col 5
                           :author "Tom Faulhaber"
                           :col 1
                           :name "clojure.pprint"
                           :doc "A Pretty Printer for Clojure\n\nclojure.pprint implements a flexible system for printing structured data\nin a pleasing easy-to-understand format. Basic use of the pretty printer is \nsimple just call pprint instead of println. More advanced users can use \nthe building blocks provided to create custom output formats. \n\nOut of the box pprint supports a simple structured format for basic data \nand a specialized format for Clojure source code. More advanced formats \nincluding formats that don't look like Clojure data at all like XML and \nJSON can be rendered by creating custom dispatch functions. \n\nIn addition to the pprint function this module contains cl-format a text \nformatting function which is fully compatible with the format function in \nCommon Lisp. Because pretty printing directives are directly integrated with\ncl-format it supports very concise custom dispatch. It also provides\na more powerful alternative to Clojure's standard format function.\n\nSee documentation for pprint and cl-format for more information or \ncomplete documentation on the Clojure web site on GitHub."
                           :name-end-row 37
                           :filename "/src/clj/clojure/pprint.clj"}]
                (adapters/analysis->libraries analysis-fixture)))))

(deftest analysis->definitions-test
  (testing "analysis -> definitions"
    (is (match? [#:definition{:defined-by "clojure.core/defmacro"
                              :library "clojure.pprint"
                              :filename "/src/clj/clojure/pprint/pprint_base.clj"
                              :macro true
                              :project "org.clojure/clojure"
                              :row 302
                              :varargs-min-arity 0
                              :added "1.2"
                              :arglist-strs ["[& args]"]
                              :col 1
                              :name-col 11
                              :end-col 16
                              :doc "Execute the body as a pretty printing logical block with output to *out* which \nmust be a pretty printing writer. When used from pprint or cl-format this can be \nassumed. \n\nThis function is intended for use when writing custom dispatch functions.\n\nBefore the body the caller can optionally specify options: :prefix :per-line-prefix \nand :suffix."
                              :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/pprint_base.clj#L302"
                              :name-end-row 302
                              :name-row 302
                              :group "org.clojure"
                              :meta {:arglists '[[options* body]]}
                              :artifact "clojure"
                              :name-end-col 31
                              :end-row 327
                              :name "pprint-logical-block"}
                 #:definition{:defined-by "clojure.core/defn"
                              :library "clojure.pprint"
                              :filename "/src/clj/clojure/pprint/print_table.clj"
                              :project "org.clojure/clojure"
                              :row 11
                              :added "1.3"
                              :arglist-strs ["[ks rows]" "[rows]"]
                              :col 1
                              :name-col 7
                              :end-col 51
                              :doc "Prints a collection of maps in a textual table. Prints table headings\n   ks and then a line of output for each row corresponding to the keys\n   in ks. If ks are not specified use the keys of the first item in rows."
                              :git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/print_table.clj#L11"
                              :fixed-arities #{1 2}
                              :name-end-row 11
                              :name-row 11
                              :group "org.clojure"
                              :meta {}
                              :artifact "clojure"
                              :name-end-col 18
                              :end-row 35
                              :name "print-table"}]
                (adapters/analysis->definitions analysis-fixture)))))
