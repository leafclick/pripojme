(ns pripojme.graph.graph
  (:require [pripojme.downloader.exporter :as exp])
  )

(defn map-from-csv [raw column]
  (map (fn [d] {:x (subs (get d 0) 0 19) :y (get d column)}) raw)
  )

(defn map-to-graph
  ([raw] (map #(str "{x: '" (:x %1) "', y: " (:y %1) "}") raw))
  ([raw group-id] (map #(str "{x: '" (:x %1) "', y: " (:y %1) ", group: " group-id "}") raw))
  )

(defn wrap-data [data]
  (str "[" (reduce str (interpose "," data)) "]"))

(defn avg [coll]
  (double (apply / (reduce (fn [[sum n] x] [(+ sum x) (inc n)]) [0 0] coll)))
  )

(defn create-average [data]
  (let [count (count data)
        middle (int (/ count 2))]
    {:x (:x (nth data middle)) :y (avg (map #(Double/parseDouble (:y %)) data))}
    )
  )

(defn interpolate-data [data time-range]
  (case (:period time-range)
    :day data
    :week (map #(create-average %) (partition-all 3 data))
    :month (map #(create-average %) (partition-all 5 data))
    )
  )

(defn csv-file [device]
  (if (= (:model device) "weather")
    (exp/path-to-weather-file)
    (exp/path-to-file (str (:model device) "-" (:devEUI device) ".csv"))
    )
  )

(defn map-all-files-in-range [devices time-range index data]
  (if (empty? devices)
    data
    (let [device (first devices)
          content (map-from-csv (exp/read-from-csv-time-range (csv-file device) time-range) (:column device))
          interpolated-data (interpolate-data content time-range)]
      (map-all-files-in-range (rest devices) time-range (inc index) (concat data (map-to-graph interpolated-data index)))))
  )

(defn construct-graph [source time-range]
  (wrap-data (map-all-files-in-range source time-range 0 []))
  )

(defn devices-to-graph [devices]
  (map #(str "{id: " (:graphId %) ", content: '" (:devEUI %) "'}") devices)
  )

(defn construct-groups [devices]
  (wrap-data (devices-to-graph devices))
  )
