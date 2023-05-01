(ns codes.clj.docs.extractor.fixtures.analysis)

(def raw
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

(def projects-adapted
  [{:project/name "org.clojure/clojure"
    :project/group "org.clojure"
    :project/artifact "clojure"
    :project/paths ["/src/main/java"
                    "/src/main/clojure"
                    "/src/resources"
                    "/src/clj"]
    :project/url "https://github.com/clojure/clojure"
    :project/tag "clojure-1.11.1"
    :project/sha "ce55092f2b2f5481d25cff6205470c1335760ef6"
    :project/manifest :pom}])

(def libraries-adapted
  [{:library/id "org.clojure/clojure/clojure.pprint"
    :library/name "clojure.pprint"
    :library/project {:project/id "org.clojure/clojure"}
    :library/group "org.clojure"
    :library/artifact "clojure"
    :library/doc "A Pretty Printer for Clojure\n\nclojure.pprint implements a flexible system for printing structured data\nin a pleasing easy-to-understand format. Basic use of the pretty printer is \nsimple just call pprint instead of println. More advanced users can use \nthe building blocks provided to create custom output formats. \n\nOut of the box pprint supports a simple structured format for basic data \nand a specialized format for Clojure source code. More advanced formats \nincluding formats that don't look like Clojure data at all like XML and \nJSON can be rendered by creating custom dispatch functions. \n\nIn addition to the pprint function this module contains cl-format a text \nformatting function which is fully compatible with the format function in \nCommon Lisp. Because pretty printing directives are directly integrated with\ncl-format it supports very concise custom dispatch. It also provides\na more powerful alternative to Clojure's standard format function.\n\nSee documentation for pprint and cl-format for more information or \ncomplete documentation on the Clojure web site on GitHub."
    :library/author "Tom Faulhaber"
    :library/filename "/src/clj/clojure/pprint.clj"
    :library/git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint.clj#L14"
    :library/meta {}
    :library/added "1.2"
    :library/name-col 5
    :library/name-end-col 19
    :library/name-end-row 37
    :library/end-col 40
    :library/end-row 39
    :library/name-row 37
    :library/row 14
    :library/col 1}])

(def definitions-adapted
  [{:definition/id "org.clojure/clojure/clojure.pprint/pprint-logical-block/0"
    :definition/name "pprint-logical-block"
    :definition/library {:library/id "org.clojure/clojure/clojure.pprint"}
    :definition/project {:project/id "org.clojure/clojure"}
    :definition/group "org.clojure"
    :definition/artifact "clojure"
    :definition/doc "Execute the body as a pretty printing logical block with output to *out* which \nmust be a pretty printing writer. When used from pprint or cl-format this can be \nassumed. \n\nThis function is intended for use when writing custom dispatch functions.\n\nBefore the body the caller can optionally specify options: :prefix :per-line-prefix \nand :suffix."
    :definition/filename "/src/clj/clojure/pprint/pprint_base.clj"
    :definition/git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/pprint_base.clj#L302"
    :definition/arglist-strs ["[& args]"]
    :definition/varargs-min-arity 0
    :definition/meta {:arglists '[[options* body]]}
    :definition/added "1.2"
    :definition/macro true
    :definition/defined-by "clojure.core/defmacro"
    :definition/name-end-row 302
    :definition/name-row 302
    :definition/name-end-col 31
    :definition/end-row 327
    :definition/name-col 11
    :definition/end-col 16
    :definition/row 302
    :definition/col 1}
   {:definition/id "org.clojure/clojure/clojure.pprint/print-table/0"
    :definition/defined-by "clojure.core/defn"
    :definition/library {:library/id "org.clojure/clojure/clojure.pprint"}
    :definition/project {:project/id "org.clojure/clojure"}
    :definition/filename "/src/clj/clojure/pprint/print_table.clj"
    :definition/row 11
    :definition/added "1.3"
    :definition/arglist-strs ["[ks rows]" "[rows]"]
    :definition/col 1
    :definition/name-col 7
    :definition/end-col 51
    :definition/doc "Prints a collection of maps in a textual table. Prints table headings\n   ks and then a line of output for each row corresponding to the keys\n   in ks. If ks are not specified use the keys of the first item in rows."
    :definition/git-source "https://github.com/clojure/clojure/blob/clojure-1.11.1/src/clj/clojure/pprint/print_table.clj#L11"
    :definition/fixed-arities #{1 2}
    :definition/name-end-row 11
    :definition/name-row 11
    :definition/group "org.clojure"
    :definition/meta {}
    :definition/artifact "clojure"
    :definition/name-end-col 18
    :definition/end-row 35
    :definition/name "print-table"}])
