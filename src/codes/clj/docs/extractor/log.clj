(ns codes.clj.docs.extractor.log
  (:import [java.time Duration LocalDateTime]))

(defmacro with-log [& body]
  `(do
     (let [executed-form# ~(str (second &form))
           start-time# (LocalDateTime/now)]
       (println executed-form# "started at" (.toString start-time#))
       (let [result# (do ~@body)
             end-time# (LocalDateTime/now)]
         (println executed-form#
                  "  ended at"
                  (.toString end-time#)
                  "took"
                  (->> (Duration/between start-time# end-time#)
                       (.getSeconds))
                  "secs")
         result#))))
