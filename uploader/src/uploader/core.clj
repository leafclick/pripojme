(ns uploader.core
  (:require [clojure.data.csv :as csv]
            [cheshire.core :refer :all]
            [clojure.java.io :as io])
  (:import (org.projecthaystack.client HClient)
           (org.projecthaystack HRef HDateTime HHisItem HNum HCoord)))

(def url "https://skyspark.leafclick.com/api/pripojme/")
(def user "...")
(def pass "...")

(defn open-client []
  (HClient/open url user pass))

(defn transform-timestamp [ts]
  (HDateTime/make (str (subs ts 0 19) "Z Prague"))
  )

(defn create-hhis-item [ts value unit]
  (do
    ;(println (str "#" value "#"))
    (HHisItem/make
      (transform-timestamp ts)
      (if (= unit "Coord")
        (HCoord/make value)
        (HNum/make (Double/parseDouble value) unit)
        )
      )
    )
  )

(defn about []
  (.about (open-client)))

(defn read-record [id]
  (.readById (open-client) (HRef/make id)))

(defn his-read [rec]
  (.hisRead (open-client) (.id rec) "2016-07-01")
  )

(defn his-to-seq [id]
  (iterator-seq (.iterator (his-read (read-record id))))
  )

(defn clear-his-write [id]
  (let [client (open-client)]
    (do
      (.eval client
             (str "hisClear(@" id ", null)"))
      (= 0 (.numRows (his-read (read-record id))))
      )
    )
  )

(defn his-write [ref values client]
  (.hisWrite client ref values)
  )

(defn his-write-single [id ts value unit]
  (his-write
    (.id (read-record id))
    (into-array HHisItem [(create-hhis-item ts value unit)])
    (open-client)
    )
  )

(defn read-from-csv [filename]
  (with-open [in-file (io/reader filename)]
    (doall
      (csv/read-csv in-file)))
  )

(defn create-batch-hhis [data column unit]
  (into [] (map #(create-hhis-item (%1 0) (%1 column) unit)) data)
  )

(defn his-write-from-file [filename column id unit]
  (let [data (read-from-csv filename)
        client (open-client)
        ref (.id (read-record id))
        clearData (filter #(not-empty (%1 column)) (rest data))]
    (map #(his-write
           ref
           (into-array HHisItem (create-batch-hhis %1 column unit))
           client)
         (partition-all 100 clearData)
         )
    )
  )

;(his-write-from-file "../downloader/RHF1S001-4786E6ED00350042.csv" 2 "1fa3a1b0-03848c48" "%")
;(.numRows (.hisRead (open-client) (.id (read-record id2)) "2016-07-01"))