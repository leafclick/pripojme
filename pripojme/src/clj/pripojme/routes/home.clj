(ns pripojme.routes.home
  (:require [pripojme.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [pripojme.graph.graph :as graph]
            [pripojme.downloader.core :as downloader]
            [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [pripojme.graph.devices :as dev])
  (:import (org.joda.time DateTime Days)))

(def projects ["CRaTechnologyRoom" "Greenhouse20"])

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
    "home.html"))

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

(defn compute-data-period [^DateTime begin ^DateTime end]
  (let [days (.getDays (Days/daysBetween begin end))]
    (cond
      (> days 7) :month
      (> days 1) :week
      :else :day)
    )
  )

(defn parse-data-imputs [request]
  (let [visjs-formatter (f/formatter "yyyy-MM-dd'T'HH:mm:ss")
        begin (f/parse visjs-formatter (subs (get-in request [:params :start]) 0 19))
        end (f/parse visjs-formatter (subs (get-in request [:params :end]) 0 19))]
    {:begin  begin
     :end    end
     :period (compute-data-period begin end)}
    )
  )

(defn cratechroom-page [params devices]
  (layout/render "cratechroom.html"
                 {:cljItems (graph/construct-graph
                              (dev/filter-checked-devices devices
                                                          (dev/add-groups-to-devices dev/devices-cratechroom
                                                                                     dev/cratechroom-temp-data)
                                                          )
                              params)
                  :devices  (dev/check-devices devices dev/devices-cratechroom)
                  :groups   (graph/construct-groups dev/devices-cratechroom)
                  :params   (params-to-web params)
                  }
                 )
  )

(defn greenhouse-page [params devices]
  (layout/render "greenhouse.html"
                 {:temperatureItems
                           (graph/construct-graph
                             (dev/filter-checked-devices devices
                                                         (dev/add-groups-to-devices dev/devices-greenhouse
                                                                                    dev/greenhouse-temp-data)
                                                         )
                             params)
                  :lightItems
                           (graph/construct-graph
                             (dev/filter-checked-devices devices
                                                         (dev/add-groups-to-devices dev/devices-greenhouse
                                                                                    dev/greenhouse-light-data)
                                                         )
                             params)
                  :humItems
                           (graph/construct-graph
                             (dev/filter-checked-devices devices
                                                         (dev/add-groups-to-devices dev/devices-greenhouse
                                                                                    dev/greenhouse-hum-data)
                                                         )
                             params)
                  :devices (dev/check-devices devices dev/devices-greenhouse)
                  :groups  (graph/construct-groups dev/devices-greenhouse)
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

(defn cratechroom-data [params devices]
  {:body {:cljItems (graph/construct-graph
                      (dev/filter-checked-devices devices
                                                  (dev/add-groups-to-devices dev/devices-cratechroom
                                                                             dev/cratechroom-temp-data)
                                                  )
                      params)}}
  )

(defn greenhouse-data [params devices]
  {:body {:temperatureItems
          (graph/construct-graph
            (dev/filter-checked-devices devices
                                        (dev/add-groups-to-devices dev/devices-greenhouse
                                                                   dev/greenhouse-temp-data)
                                        )
            params)
          :lightItems
          (graph/construct-graph
            (dev/filter-checked-devices devices
                                        (dev/add-groups-to-devices dev/devices-greenhouse
                                                                   dev/greenhouse-light-data)
                                        )
            params)
          :humItems
          (graph/construct-graph
            (dev/filter-checked-devices devices
                                        (dev/add-groups-to-devices dev/devices-greenhouse
                                                                   dev/greenhouse-hum-data)
                                        )
            params)}}
  )

(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/update" [] (update-data))
           (GET "/cratechroom" [] (cratechroom-page (create-defaults) (map #(%1 :devEUI) dev/devices-cratechroom)))
           (POST "/cratechroom" request (cratechroom-page (parse-imputs request) (get-in request [:params :devices])))
           (POST "/cratechroomData" request (cratechroom-data (parse-data-imputs request) (get-in request [:params :devices])))
           (GET "/greenhouse" [] (greenhouse-page (create-defaults) (map #(%1 :devEUI) dev/devices-greenhouse)))
           (POST "/greenhouse" request (greenhouse-page (parse-imputs request) (get-in request [:params :devices])))
           (POST "/greenhouseData" request (greenhouse-data (parse-data-imputs request) (get-in request [:params :devices])))
           )
