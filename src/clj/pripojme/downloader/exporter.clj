(ns pripojme.downloader.exporter
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-time.format :as f]
            [pripojme.config :refer [env]]
            [clojure.tools.logging :as log])
  )

(defn path-to-file [filename]
  (let [path (:data-dir env)]
    (if (nil? path)
      filename
      (if (.endsWith path "/")
        (str path filename)
        (str path "/" filename)
        )
      )
    )
  )

(defn path-to-weather-file []
  (let [path (:weather-file env)]
    (if (nil? path)
      (path-to-file "weather-prague-2016.csv")
      path
      )
    )
  )

(defn file-exists [filename]
  (.exists (io/as-file (path-to-file filename)))
  )

(defn write-to-csv [filename data]
  (with-open [out-file (io/writer (path-to-file filename))]
    (csv/write-csv out-file
                   data))
  )

(defn append-to-csv [filename data]
  (with-open [out-file (io/writer (path-to-file filename) :append true)]
    (csv/write-csv out-file
                   data))
  )

(defn read-from-csv
  "Returns csv contents or nil if file can't be read."
  [filename]
  (try
    (with-open [in-file (io/reader filename)]
      (doall
        (csv/read-csv in-file)))
    (catch Exception e
      (log/error "Can't read CSV file" filename ":" e))))

(defn read-from-csv-time-range [file time-range]
  (let [begin (:begin time-range)
        end (:end time-range)
        formatter (f/formatters :date-time-no-ms)]
    (take-while
      #(.isBefore (f/parse formatter (nth %1 0)) end)
      (drop-while
        #(.isBefore (f/parse formatter (nth %1 0)) begin)
        (rest (read-from-csv file)))
      )
    )
  )
