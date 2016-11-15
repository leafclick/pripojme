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

(defn map-to-data [raw group-id]
  (map (fn [data] {:x (:x data) :y (:y data) :group group-id}) raw)
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

(defn interpolate-data [data]
  (let [count (count data)]
    (if (< count 200)
      data
      (map #(create-average %) (partition-all (int (/ count 100)) data))
      )
    )
  )

(defn csv-file [device]
  (if (= (:model device) "weather")
    (exp/path-to-weather-file)
    (exp/path-to-file (str (:model device) "-" (:devEUI device) ".csv"))
    )
  )

(defn filter-data-if-needed [pred data]
  (if (nil? pred)
    data
    (filter pred data))
  )

(defn map-all-files-in-range [devices time-range mapper filter data]
  (if (empty? devices)
    data
    (let [device (first devices)
          content (map-from-csv (exp/read-from-csv-time-range (csv-file device) time-range) (:column device))
          interpolated-data (interpolate-data (filter-data-if-needed filter content))]
      (map-all-files-in-range (rest devices) time-range mapper filter (concat data (mapper interpolated-data (:graphId device))))))
  )

(defn construct-graph [source time-range]
  (wrap-data (map-all-files-in-range source time-range map-to-graph nil []))
  )

(defn construct-data [source time-range]
  (map-all-files-in-range source time-range map-to-data nil [])
  )

(defn construct-filtered-graph [source time-range filter]
  (wrap-data (map-all-files-in-range source time-range map-to-graph filter []))
  )

(defn construct-filtered-data [source time-range filter]
  (map-all-files-in-range source time-range map-to-data filter [])
  )

(defn map-all-files-in-range-no-interpolation [devices time-range mapper filter data]
  (if (empty? devices)
    data
    (let [device (first devices)
          content (map-from-csv (exp/read-from-csv-time-range (csv-file device) time-range) (:column device))
          filtered-data (filter-data-if-needed filter content)]
      (map-all-files-in-range-no-interpolation (rest devices) time-range mapper filter (concat data (mapper filtered-data (:graphId device))))))
  )

(defn construct-filtered-no-interpolation-graph [source time-range filter]
  (wrap-data (map-all-files-in-range-no-interpolation source time-range map-to-graph filter []))
  )

(defn construct-filtered-no-interpolation-data [source time-range filter]
  (map-all-files-in-range-no-interpolation source time-range map-to-data filter [])
  )

(defn devices-to-graph [devices]
  (map #(str "{id: " (:graphId %) ", content: '" (:description %) " (" (:devEUI %) ")', className: '" (str "vis-graph-group" (:graphId %)) "'}") devices)
  )

(defn construct-groups [devices]
  (wrap-data (devices-to-graph devices))
  )
