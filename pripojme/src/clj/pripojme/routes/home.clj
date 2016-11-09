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
    :vendor      "Develict"}])

(defn create-defaults []
  (let [today (.toDateTime (t/today-at-midnight))]
    {:begin  (.minusWeeks today 1)
     :end    today
     :period :week}
    )
  )

(defn params-to-web [params]
  (let [formatter (f/formatter "dd.MM.yyyy")]
    {:begin  (f/unparse formatter (:begin params))
     :end    (f/unparse formatter (:end params))
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

(defn compute-end [^DateTime start period]
  (case period
    :day (.plusDays start 1)
    :week (.plusWeeks start 1)
    :months (.plusMonths start 1)
    )
  )

(defn parse-imputs [request]
  (let [formatter (f/formatter "dd.MM.yyyy")
        date (get-in request [:params :date])
        period (keyword (get-in request [:params :period]))
        start (f/parse formatter date)
        end (compute-end start period)
        ]
    {:begin  start
     :end    end
     :period period}
    )
  )

(defn filter-checked-devices [checked-devices possible-devices]
  (filter #(some (fn [name] (.contains (%1 :file) name)) checked-devices) possible-devices)
  )

(defn cratechroom-page [request]
  (let [params (parse-imputs request)
        devices (get-in request [:params :devices])]
    (layout/render "cratechroom.html" {:cljItems (graph/construct-graph
                                                   (filter-checked-devices devices
                                                                           [{:file "DTH-0018B20000066679.csv" :column 1}
                                                                            {:file "DTH-0018B20000066681.csv" :column 1}])
                                                   params)
                                       }
                   )
    )
  )

(defn greenhouse-page [request]
  (let [params (parse-imputs request)
        devices (get-in request [:params :devices])]
    (layout/render "greenhouse.html"
                   {:temperatureItems
                    (graph/construct-graph
                      (filter-checked-devices devices [{:file "DeSense-0004A30B001A180C.csv" :column 1}
                                                       {:file "DeSense-0004A30B0019BE42.csv" :column 1}
                                                       {:file "DeSense-0004A30B00196841.csv" :column 1}
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
                    }
                   )
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
           (GET "/cratechroom" [] (cratechroom-page nil))
           (POST "/cratechroom" request (cratechroom-page request))
           (GET "/greenhouse" [] (greenhouse-page nil))
           (POST "/greenhouse" request (greenhouse-page request))
           (GET "/about" [] (about-page)))
