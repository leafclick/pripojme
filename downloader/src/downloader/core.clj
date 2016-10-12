(ns downloader.core
  (:require [clj-http.client :as http]
            [cheshire.core :refer :all]
            [downloader.dispatch :as disp]
            [downloader.exporter :as exp])
  )

(def token "lalEH9j8oXC48RFf2pR0B57ZsZj68rS2")

(defn fetch-projects []
  "fetch list of projects"
  (
    http/get "http://api.pripoj.me/project/get"
             {
              :accept       :json,
              :as           :json-string-keys,
              :query-params {
                             "token" token,
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
                             "token" token,
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
                             "token"  token,
                             "order"  "asc",
                             "limit"  limit,
                             "offset" offset
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
  ; poladit strankovani, toto skace prvek 1000? a nasobky...
  (let [page (extract-data-from-response (fetch-device-data device-id 10 (* index 10)))]
    (if (or (< 5 index) (< 10 (count page)))
      (concat data page)
      (do
        (Thread/sleep 1000)
        (load-all-data device-id (inc index) (concat data page)))
      )
    )
  )

(defn parse-data [model rawData]
  (map (fn [raw] (conj {:timestamp (:timestamp raw)} (disp/parse-payload model (:payloadHex raw)))) rawData))

(defn export-device-data [model device-id]
  "export all data for device into csv file"
  (let [data (parse-data model (load-all-data device-id 0 []))]
    (exp/write-to-csv (str device-id ".csv") (concat [(disp/get-column-names model)] (map #(select-values %1 (map keyword (disp/get-column-names model))) data)))
    )
  )
