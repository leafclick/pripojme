(ns pripojme.downloader.core
  (:require [clj-http.client :as http]
            [cheshire.core :refer :all]
            [pripojme.downloader.dispatch :as disp]
            [pripojme.downloader.exporter :as exp]
            [pripojme.config :refer [env]]
            [clojure.tools.logging :as log])
  )

(defn fetch-projects []
  "fetch list of projects"
  (
    http/get "http://api.pripoj.me/project/get"
             {
              :accept       :json,
              :as           :json-string-keys,
              :query-params {
                             "token" (:token env),
                             }
              }
             )
  )

(defn fetch-project-ids []
  "list all projects ids"
  (map #(%1 "projectId") ((:body (fetch-projects)) "records"))
  )

(defn fetch-devices [project]
  "fetch devices registered in project"
  (
    http/get (str "http://api.pripoj.me/device/get/" project)
             {:accept       :json,
              :as           :json-string-keys,
              :query-params {
                             "token" (:token env),
                             }
              }
             )
  )

(defn fetch-all-devices []
  "fetch all devices for all projects"
  (reduce concat (map #((:body (fetch-devices %1)) "records") (fetch-project-ids)))
  )

(defn fetch-device-models []
  "fetch all unique device models"
  (into #{} (map #(%1 "model") (fetch-all-devices)))
  )

(defn fetch-device-data
  "fetch data from given device"
  ([device-id limit] (fetch-device-data device-id limit 0))
  ([device-id limit offset]
   (http/get (str "http://api.pripoj.me/message/get/" device-id)
             {:accept       :json,
              :as           :json-string-keys,
              :query-params {
                             "token"  (:token env),
                             "order"  "asc",
                             "limit"  limit,
                             "offset" offset
                             }}
             )
    )
  ([device-id limit offset start]
   (http/get (str "http://api.pripoj.me/message/get/" device-id)
             {:accept       :json,
              :as           :json-string-keys,
              :query-params {
                             "token"  (:token env),
                             "order"  "asc",
                             "limit"  limit,
                             "offset" offset,
                             "start"  start
                             }}
             )
    )
  )

(defn extract-data-from-response [resp]
  (map (fn [rec] {:timestamp (get rec "createdAt") :payloadHex (get rec "payloadHex")}) (get (:body resp) "records")
       )
  )

(defn select-values [map ks]
  "return values from map in fixed order"
  (reduce #(conj %1 (map %2)) [] ks))

(defn load-all-data [device-id index data]
  "load data by 1000 at a time with 100 ms delay"
  (let [page (extract-data-from-response (fetch-device-data device-id 1000 (* index 1000)))]
    (if (> 1000 (count page))
      (concat data page)
      (do
        (Thread/sleep 100)
        (load-all-data device-id (inc index) (concat data page)))
      )
    )
  )

(defn load-all-data-from [device-id index data start]
  "load data by 1000 at a time with 100 ms delay"
  (let [page (extract-data-from-response (fetch-device-data device-id 1000 (* index 1000) start))]
    (if (> 1000 (count page))
      (concat data page)
      (do
        (Thread/sleep 100)
        (load-all-data-from device-id (inc index) (concat data page) start))
      )
    )
  )

(defn parse-payload [model raw]
  (let [payloadHex (:payloadHex raw)]
    (if-let [payload (disp/parse-payload model payloadHex)]
      (conj {:timestamp (:timestamp raw)} payload)
      (log/warn (str "Malformed data for payload" model " #" payloadHex "#")))))

(defn parse-data [model rawData]
  (let [xf (comp (map (fn [raw] (parse-payload model raw)))
                 (remove nil?))]
    (into [] xf rawData)
    )
  )

(defn csv-name [model device-id]
  (str model "-" device-id ".csv")
  )

(defn export-device-data [model device-id]
  "export all data for device into csv file"
  (let [data (parse-data model (load-all-data device-id 0 []))]
    (exp/write-to-csv (csv-name model device-id)
                      (concat [(disp/get-column-names model)]
                              (map #(select-values %1 (map keyword (disp/get-column-names model))) data)))
    )
  )

(defn export-project-devices-data [project-id]
  "export all data for all devices from project"
  (let [devices ((:body (fetch-devices project-id)) "records")]
    (map #(export-device-data (%1 "model") (%1 "devEUI")) devices)
    )
  )

(defn append-device-data [model device-id]
  (let [filename (csv-name model device-id)
        last-record (last (exp/read-from-csv (exp/path-to-file filename)))
        data (parse-data model (rest (load-all-data-from device-id 0 [] (subs (get last-record 0) 0 19))))]
    (exp/append-to-csv filename
                       (map #(select-values %1 (map keyword (disp/get-column-names model))) data))
    )
  )

(defn update-device-data [model device-id]
  (let [filename (csv-name model device-id)]
    (if (exp/file-exists filename)
      (append-device-data model device-id)
      (export-device-data model device-id)
      )
    )
  )

(defn update-project-data [project-id]
  (doseq [device ((:body (fetch-devices project-id)) "records")]
    (update-device-data (device "model") (device "devEUI")))
  )