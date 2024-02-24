(ns dev.playground
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [codes.clj.docs.extractor.core :as core]
            [codes.clj.docs.extractor.datalevin :as datalevin]
            [datalevin.core :as d]
            [datalevin.search-utils :as su]
            [datalevin.util :as util])
  (:import [java.io File]))

(defn get-url [git-url]
  (-> (http/get git-url {:as :json})
      :body))

(defn download-unzip [dir url]
  (let [stream (-> (http/get url {:as :byte-array})
                   :body
                   io/input-stream
                   java.util.zip.ZipInputStream.)]
    (loop [file-data (.getNextEntry stream)]
      (when file-data
        (let [file-path (str dir File/separatorChar (.getName file-data))
              saveFile (File. file-path)]
          (if (.isDirectory file-data)
            (when-not (.exists saveFile)
              (.mkdirs saveFile))
            (let [parentDir (File. (.substring file-path 0 (.lastIndexOf file-path (int File/separatorChar))))]
              (when-not (.exists parentDir)
                (.mkdirs parentDir))
              (io/copy stream saveFile))))
        (recur (.getNextEntry stream))))))

(comment
  ; reset database & download unzip from releases
  (let [dir "target/docs-db"]
    (println "deleting")
    (try (util/delete-files dir) (catch Exception _))
    (println "downloading")
    (->> "https://api.github.com/repos/clj-codes/docs.extractor/releases/latest"
         get-url
         :tag_name
         (format "https://github.com/clj-codes/docs.extractor/releases/download/%s/docs-db.zip")
         (download-unzip dir)))

  ; reset database & generate new database
  (let [dir "target/docs-db"]
    (println "deleting")
    (try (util/delete-files dir) (catch Exception _))
    (println "bulking")
    (core/extract! {}))

  ; fulltext search with generated database
  (let [conn (d/get-conn "target/docs-db"
                         datalevin/db-schemas)

        db (d/db conn)

        result (doall (d/q '[:find ?e ?a ?v ;(pull ?e [*])
                             :in $ ?q
                             :where
                             [(fulltext $ ?q {:top 30
                                              :domains ["definition-name"]})
                              [[?e ?a ?v]]]]
                           db
                           "a"))]
    (d/close conn)
    result)

  ; fulltext raw search with generated database
  (let [lmdb (d/open-kv "target/docs-db")
        engine (d/new-search-engine lmdb {:query-analyzer (su/create-analyzer
                                                           {:tokenizer (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+")
                                                            :token-filters [su/lower-case-token-filter]})
                                          :include-text?   true
                                          :domain "definition-name"})
        result (doall (d/search engine "a" {:top 30}))]
    (d/close-kv lmdb)
    result)

  ; simple query definition by name
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find (pull ?e [* {:definition/namespace [* {:namespace/project [*]}]}])
                             :in $ ?q
                             :where
                             [?e :definition/name ?q]]
                           db
                           "file-position"))]
    (d/close conn)
    result)

  ; regex searching
  (let [conn (d/get-conn "target/docs-db"
                         datalevin/db-schemas)
        db (d/db conn)
        result (doall (->> (d/q '[:find [(pull ?e [*]) ...]
                                  :in $ ?q
                                  :where
                                  [(str ".*" ?q ".*") ?pattern]
                                  [(re-pattern ?pattern) ?regex]
                                  [(re-matches ?regex ?name)]
                                  [?e :definition/name ?name]]
                                db
                                "pending")
                           (sort-by (juxt
                                     :definition/id
                                     :definition/name))))]
    (d/close conn)
    result)

  ; count all data
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find (count ?e)
                             :in $
                             :where [?e]]
                           db))]
    (d/close conn)
    result)

  ; tests with fulltext search
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find ?e ?name ?a ?v
                             :in $ ?q
                             :where
                             [(fulltext $ ?q) [[?e ?a ?v]]]
                             [?e :definition/name ?name]]
                           db
                           "assoc"))]
    (d/close conn)
    result)

  ; tests with fulltext and analyzer
  (let [query-analyzer (su/create-analyzer
                        {:tokenizer (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+")
                         :token-filters [su/lower-case-token-filter]})
        analyzer (su/create-analyzer
                  {:tokenizer (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+")
                   :token-filters [su/lower-case-token-filter
                                   su/prefix-token-filter]})

        dir  "/tmp/mydb"
        conn (d/create-conn dir
                            {:text {:db/valueType :db.type/string
                                    :db/fulltext  true
                                    :db.fulltext/domains ["txt"]}}
                            {:search-domains {"txt" {:analyzer analyzer
                                                     :query-analyzer query-analyzer}}})

        data [{:text "abs"}
              {:text "assoc!"}
              {:text "assoc"}
              {:text "assoc-in"}
              {:text "assoc-dom"}
              {:text "assoc-meta"}
              {:text "associative?"}]

        _transact (d/transact! conn data)

        result (d/q '[:find ?e ?a ?v
                      :in $ ?q
                      :where [(fulltext $ ?q {:domains ["txt"]
                                              :display :refs}) [[?e ?a ?v]]]]
                    (d/db conn)
                    "a")]

    (d/close conn)
    (util/delete-files dir)

    result)

  ; tests with fulltext and analyzer on a raw query
  (let [query-analyzer (su/create-analyzer
                        {:tokenizer (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+")
                         :token-filters [su/lower-case-token-filter]})

        analyzer (su/create-analyzer
                  {:tokenizer (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+")
                   :token-filters [su/lower-case-token-filter
                                   su/prefix-token-filter]})

        lmdb (d/open-kv "/tmp/mydb")

        engine (d/new-search-engine lmdb {:query-analyzer query-analyzer
                                          :analyzer analyzer
                                          :include-text?   true
                                          :domain "definition-name"})
        input {1 "abs"
               2 "assoc!"
               3 "assoc"
               4 "assoc-in"
               5 "assoc-dom"
               6 "assoc-meta"
               7 "associative?"}

        _transact (doseq [[k v] input]
                    (d/add-doc engine k v))

        result (doall (d/search engine "a" {:top 20 :display :texts}))]

    (d/close-kv lmdb)

    result))




