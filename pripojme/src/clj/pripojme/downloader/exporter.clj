(ns pripojme.downloader.exporter
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io])
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