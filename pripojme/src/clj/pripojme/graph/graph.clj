(ns pripojme.graph.graph
  (:require [pripojme.downloader.exporter :as exp])
  )

(defn map-from-csv [raw column]
  (map (fn [d] {:x (subs (get d 0) 0 19) :y (get d column)}) raw)
  )

(defn map-to-graph
  ([raw] (map #(str "{x: \"" (:x %1) "\", y: " (:y %1) "}") raw))
  ([raw group-id] (map #(str "{x: \"" (:x %1) "\", y: " (:y %1) ", group: " group-id "}") raw))
  )

(defn wrap-data [data]
  (str "[" (reduce str (interpose "," data)) "]"))

(defn map-all-files [devices index data]
  (if (empty? devices)
    data
    (let [device (first devices)
          content (map-from-csv (rest (exp/read-from-csv (:file device))) (:column device))]
      (map-all-files (rest devices) (inc index) (concat data (map-to-graph content index)))))
  )

(defn construct-graph [source]
  (wrap-data (map-all-files source 0 []))
  )

;(defn construct-graph [source time-range]
 ; (wrap-data (map-all-files source 0 []))
  ;)
