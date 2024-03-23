(ns dev.playground
  (:require [clj-http.client :as http]
            [clojure.java.io :as io]
            [codes.clj.docs.extractor.config :as config]
            [codes.clj.docs.extractor.core :as core]
            [codes.clj.docs.extractor.datalevin :as datalevin]
            [datalevin.core :as d]
            [datalevin.interpret :refer [inter-fn]]
            [datalevin.search-utils :as su]
            [datalevin.util :as util])
  (:import [java.io File]))

(defn get-url [git-url]
  (-> (http/get git-url {;:headers {"Authorization" "Bearer token"}
                         :as :json})
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

(defn get-github-data [owner-repository]
  (let [tags (get-url
              (str "https://api.github.com/repos/"
                   owner-repository
                   "/tags"))
        latest (first tags)
        latest-hash (get-url
                     (str "https://api.github.com/repos/"
                          owner-repository
                          "/git/ref/tags/"
                          (:name latest)))]
    (merge latest latest-hash)))

(defn get-git-deps-info [owner-repository]
  (let [{:keys [name object]} (-> owner-repository
                                  get-github-data
                                  (select-keys [:name :object]))]
    {:project/name ""
     :git/url (str "https://github.com/" owner-repository)
     :git/tag name
     :git/sha (:sha object)}))

(comment
  ; getting data from github
  (mapv get-git-deps-info
        ["dakrone/clj-http"
         "dakrone/cheshire"])

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

  (defn >> [a] (prn a) a)

  ; fulltext search with generated database
  (let [conn (d/get-conn "target/docs-db"
                         datalevin/db-schemas
                         {:search-domains {"project-name" {:query-analyzer datalevin/query-analyzer
                                                           :analyzer datalevin/analyzer}
                                           "namespace-name" {:query-analyzer datalevin/query-analyzer
                                                             :analyzer datalevin/analyzer}
                                           "definition-name" {:query-analyzer datalevin/query-analyzer
                                                              :analyzer datalevin/analyzer}}})

        db (d/db conn)

        datoms (->> (d/fulltext-datoms db
                                       "a"
                                       {:domains ["definition-name"
                                                  "namespace-name"
                                                  "project-name"]})
                    (map first)
                    (d/pull-many db '[:definition/id
                                      :definition/name
                                      :definition/doc
                                      :namespace/id
                                      :namespace/name
                                      :namespace/doc
                                      :project/id
                                      :project/artifact
                                      :project/group]))]
    (d/close conn)
    datoms)

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

  ; count deps in config
  (count (:deps (config/read! "resources/config.edn")))

  ; count by project
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find ?pn ?pg (count ?d)
                             :in $
                             :where
                             [?p :project/id]
                             [?p :project/group ?pg]
                             [?p :project/name ?pn]
                             [?p :project/sha ?ps]
                             [?n :namespace/project ?p]
                             [?d :definition/namespace ?n]]
                           db))]
    (d/close conn)
    result)

; tests with fulltext search
  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find ?e ?name ?a ?v ?b ?d
                             :in $ ?q
                             :where
                             [(fulltext $ ?q) [[?e ?a ?v ?b ?d]]]
                             [?e :definition/name ?name]]
                           db
                           "astoc"))]
    (d/close conn)
    result)

  (let [conn (d/get-conn "target/docs-db" datalevin/db-schemas)
        db (d/db conn)
        result (doall (d/q '[:find ?e ?a
                             :in $
                             :where
                             [?e :definition/deprecated ?a]]
                           db))]
    (d/close conn)
    result)

  ; tests with fulltext and analyzer
  (let [query-analyzer (su/create-analyzer
                        {:tokenizer (datalevin/merge-tokenizers
                                     (inter-fn [s] [[s 0 0]])
                                     (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
                         :token-filters [su/lower-case-token-filter]})

        analyzer (su/create-analyzer
                  {:tokenizer (datalevin/merge-tokenizers
                               (inter-fn [s] [[s 0 0]])
                               (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
                   :token-filters [su/lower-case-token-filter
                                   su/prefix-token-filter]})

        dir  (str "/tmp/mydb-" (random-uuid))
        conn (d/create-conn dir
                            {:text {:db/valueType :db.type/string
                                    :db/fulltext  true
                                    :db.fulltext/domains ["txt"]}}
                            {:search-domains {"txt" {:analyzer analyzer
                                                     :query-analyzer query-analyzer}}})

        data [{:text "a"}
              {:text "abs"}
              {:text "assoc!"}
              {:text "assoc"}
              {:text "assoc-in"}
              {:text "assoc-dom"}
              {:text "assoc-meta"}
              {:text "associative?"}
              {:text "b"}
              {:text "ba"}
              {:text "bas"}
              {:text "*"}
              {:text "/"}
              {:text "->"}
              {:text "->>"}
              {:text "as->"}
              {:text "."}
              {:text "as->banana"}]

        _transact (d/transact! conn data)

        result (->> (d/q '[:find ?e ?v
                           :in $ ?q
                           :where
                           [(fulltext $ ?q {:top 20}) [[?e ?a ?v]]]]
                         (d/db conn)
                         "as")
                    doall)]

    (d/close conn)
    (util/delete-files dir)

    result)

  ; tests with fulltext and analyzer on a raw query
  (let [query-analyzer (su/create-analyzer
                        {:tokenizer (datalevin/merge-tokenizers
                                     (inter-fn [s] [[s 0 0]])
                                     (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
                         :token-filters [su/lower-case-token-filter]})

        analyzer (su/create-analyzer
                  {:tokenizer (datalevin/merge-tokenizers
                               (inter-fn [s] [[s 0 0]])
                               (su/create-regexp-tokenizer #"[\s:/\.;,!=?\"'()\[\]{}|<>&@#^*\\~`\-]+"))
                   :token-filters [su/lower-case-token-filter
                                   su/prefix-token-filter]})

        dir  (str "/tmp/lmdb-" (random-uuid))
        lmdb (d/open-kv dir)

        engine (d/new-search-engine lmdb {:query-analyzer query-analyzer
                                          :analyzer analyzer
                                          :include-text?   true
                                          :domain "definition-name"})
        input {0 "a"
               1 "abs"
               2 "assoc!"
               3 "assoc"
               4 "assoc-in"
               5 "assoc-dom"
               6 "assoc-meta"
               7 "associative?"
               8 "b"
               9 "ba"
               10 "bas"
               11 "->"
               12 "->>"
               13 "as->"
               14 "as->banana"
               15 "/"
               16 "*"
               17 "."}

        _transact (doseq [[k v] input]
                    (d/add-doc engine k v))

        result (doall (d/search engine "->" {:top 20 :display :texts}))]

    (d/close-kv lmdb)
    (util/delete-files dir)

    result))
