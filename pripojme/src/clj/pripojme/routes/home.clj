(ns pripojme.routes.home
  (:require [pripojme.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [pripojme.graph.graph :as graph]
            [pripojme.downloader.core :as downloader]
            [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:import (org.joda.time DateTime)))

(def projects ["CRaTechnologyRoom" "Greenhouse20"])

(def devices-cratechroom
  [{:devEUI      "0018B20000066679",
    :projectId   "CRaTechnologyRoom",
    :description "Teplotni a vlhkostni cidlo",
    :model       "DTH",
    :vendor      "Solidus Tech"}
   {:devEUI      "0018B20000066681",
    :projectId   "CRaTechnologyRoom",
    :description "Teplotni a vlhkostni cidlo",
    :model       "DTH",
    :vendor      "Solidus Tech"}
   {:devEUI      "prague-2016",
    :projectId   "Weather",
    :description "Teplotni udaje Praha 2016",
    :model       "weather",
    :vendor      "NA"}]
  )

(def devices-greenhouse
  [{:devEUI      "0004A30B0019BE42",
    :projectId   "Greenhouse20",
    :description "Teplotni a vlhkostni cidlo (vzduch)",
    :model       "DeSense",
    :vendor      "Develict"}
   {:devEUI      "0004A30B001A180C",
    :projectId   "Greenhouse20",
    :description "Teplotni a vlhkostni cidlo (vzduch)",
    :model       "DeSense",
    :vendor      "Develict"}
   {:devEUI      "0004A30B00196841",
    :projectId   "Greenhouse20",
    :description "Teplotni a vlhkostni cidlo (vzduch)",
    :model       "DeSense",
    :vendor      "Develict"}
   {:devEUI      "0004A30B0019810D",
    :projectId   "Greenhouse20",
    :description "Cidlo vodivosti pudy (zadni cast skleniku)",
    :model       "DeSenseSoil",
    :vendor      "Develict"}
   {:devEUI      "0004A30B0019F784",
    :projectId   "Greenhouse20",
    :description "Cidlo vodivosti pudy (predni cast skleniku)",
    :model       "DeSenseSoil",
    :vendor      "Develict"}
   {:devEUI      "0004A30B0019DD02",
    :projectId   "Greenhouse20",
    :description "Cidlo intenzity svetla (zadni cast skleniku)",
    :model       "DeSenseLight",
    :vendor      "Develict"}
   {:devEUI      "0004A30B00199EB1",
    :projectId   "Greenhouse20",
    :description "Cidlo intenzity svetla (predni cast skleniku)",
    :model       "DeSenseLight",
    :vendor      "Develict"}
   {:devEUI      "prague-2016",
    :projectId   "Weather",
    :description "Teplotni udaje Praha 2016",
    :model       "weather",
    :vendor      "NA"}]
  )

(defn create-defaults []
  (let [today (.toDateTime (t/today-at-midnight))]
    {:begin  (.minusWeeks today 1)
     :end    today
     :period :week}
    )
  )

(defn params-to-web [params]
  (let [formatter (f/formatter "dd.MM.yyyy")
        visjs-formatter (f/formatter "yyyy-MM-dd 00:00:00")]
    {:begin  (f/unparse formatter (:begin params))
     :end    (f/unparse visjs-formatter (:end params))
     :start  (f/unparse visjs-formatter (:begin params))
     :period (name (:period params))
     }
    )
  )

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn check-device [checked-devices device]
  (if (some (fn [name] (= (:devEUI device) name)) checked-devices)
    (conj device {:checked "checked"})
    device
    )
  )

(defn check-devices [checked-devices devices]
  (map #(check-device checked-devices %1) devices)
  )

(defn about-page []
  (layout/render "about.html" {:projects projects
                               :devices  (check-devices ["0004A30B00199EB1" "0004A30B0019810D"] devices-greenhouse)
                               :params   (params-to-web (create-defaults))}))

(defn plus-period [^DateTime start period]
  (case period
    :day (.plusDays start 1)
    :week (.plusWeeks start 1)
    :month (.plusMonths start 1)
    )
  )

(defn minus-period [^DateTime start period]
  (case period
    :day (.minusDays start 1)
    :week (.minusWeeks start 1)
    :month (.minusMonths start 1)
    )
  )

(defn compute-start [^DateTime start paging period]
  (if (nil? paging)
    start
    (case paging
      :previous (minus-period start period)
      :next (plus-period start period)
      start
      ))
  )

(defn parse-imputs [request]
  (let [formatter (f/formatter "dd.MM.yyyy")
        date (get-in request [:params :date])
        period (keyword (get-in request [:params :period]))
        paging (keyword (get-in request [:params :paging]))
        start (compute-start (f/parse formatter date) paging period)
        end (plus-period start period)
        ]
    {:begin  start
     :end    end
     :period period}
    )
  )

(defn filter-checked-devices [checked-devices possible-devices]
  (filter #(some (fn [name] (.contains (%1 :file) name)) checked-devices) possible-devices)
  )

(defn cratechroom-page [params devices]
  (layout/render "cratechroom.html" {:cljItems (graph/construct-graph
                                                 (filter-checked-devices devices
                                                                         [{:file "DTH-0018B20000066679.csv" :column 1}
                                                                          {:file "DTH-0018B20000066681.csv" :column 1}
                                                                          {:file "weather-prague-2016.csv" :column 1}])
                                                 params)
                                     :devices  (check-devices devices devices-cratechroom)
                                     :params   (params-to-web params)
                                     }
                 )
  )

(defn greenhouse-page [params devices]
  (layout/render "greenhouse.html"
                 {:temperatureItems
                           (graph/construct-graph
                             (filter-checked-devices devices [{:file "DeSense-0004A30B001A180C.csv" :column 1}
                                                              {:file "DeSense-0004A30B0019BE42.csv" :column 1}
                                                              {:file "DeSense-0004A30B00196841.csv" :column 1}
                                                              {:file "weather-prague-2016.csv" :column 1}
                                                              ])
                             params)
                  :lightItems
                           (graph/construct-graph
                             (filter-checked-devices devices [{:file "DeSenseLight-0004A30B0019DD02.csv" :column 1}
                                                              {:file "DeSenseLight-0004A30B00199EB1.csv" :column 1}
                                                              ])
                             params)
                  :humItems
                           (graph/construct-graph
                             (filter-checked-devices devices [{:file "DeSenseSoil-0004A30B0019F784.csv" :column 1}
                                                              {:file "DeSenseSoil-0004A30B0019810D.csv" :column 1}
                                                              {:file "DeSense-0004A30B001A180C.csv" :column 2}
                                                              {:file "DeSense-0004A30B0019BE42.csv" :column 2}
                                                              {:file "DeSense-0004A30B00196841.csv" :column 2}
                                                              ])
                             params)
                  :devices (check-devices devices devices-greenhouse)
                  :params  (params-to-web params)
                  }
                 )
  )

(defn update-data []
  (do
    (future
      (doseq [project projects]
        (downloader/update-project-data project)
        ))
    (layout/render "update.html" {:projects projects})
    )
  )

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/update" [] (update-data))
           (GET "/cratechroom" [] (cratechroom-page (create-defaults) (map #(%1 :devEUI) devices-cratechroom)))
           (POST "/cratechroom" request (cratechroom-page (parse-imputs request) (get-in request [:params :devices])))
           (GET "/greenhouse" [] (greenhouse-page (create-defaults) (map #(%1 :devEUI) devices-greenhouse)))
           (POST "/greenhouse" request (greenhouse-page (parse-imputs request) (get-in request [:params :devices])))
           (GET "/about" [] (about-page)))
