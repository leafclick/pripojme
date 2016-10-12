(ns downloader.exporter
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  )

(defn write-to-csv [filename data]
  (with-open [out-file (io/writer filename)]
    (csv/write-csv out-file
                   data))
  )
