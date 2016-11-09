(ns pripojme.downloader.exporter
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-time.format :as f])
  )

(defn file-exists [filename]
  (.exists (io/as-file filename))
  )

(defn write-to-csv [filename data]
  (with-open [out-file (io/writer filename)]
    (csv/write-csv out-file
                   data))
  )

(defn append-to-csv [filename data]
  (with-open [out-file (io/writer filename :append true)]
    (csv/write-csv out-file
                   data))
  )

(defn read-from-csv [filename]
  (with-open [in-file (io/reader filename)]
    (doall
      (csv/read-csv in-file)))
  )

(defn read-from-csv-time-range [filename time-range]
  (let [begin (:begin time-range)
        end (:end time-range)
        formatter (f/formatters :date-time-no-ms)]
    (take-while
      #(.isBefore (f/parse formatter (nth %1 0)) end)
      (drop-while
        #(.isBefore (f/parse formatter (nth %1 0)) begin)
        (rest (read-from-csv filename)))
      )
    )
  )
