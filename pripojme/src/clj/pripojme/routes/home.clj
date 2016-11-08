(ns pripojme.routes.home
  (:require [pripojme.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [pripojme.graph.graph :as graph]
            [pripojme.downloader.core :as downloader]
            [clojure.java.io :as io]))

(def projects ["CRaTechnologyRoom" "Greenhouse20"])

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page []
  (layout/render "about.html" {:projects projects}))

(defn cratechroom-page []
  (layout/render "cratechroom.html" {:cljItems (graph/construct-graph
                                                 [{:file "DTH-0018B20000066679.csv" :column 1}
                                                  {:file "DTH-0018B20000066681.csv" :column 1}])}))

(defn greenhouse-page []
  (layout/render "greenhouse.html"
                 {:temperatureItems
                  (graph/construct-graph
                    [{:file "DeSense-0004A30B001A180C.csv" :column 1}
                     {:file "DeSense-0004A30B0019BE42.csv" :column 1}
                     {:file "DeSense-0004A30B00196841.csv" :column 1}
                     ])
                  :lightItems
                  (graph/construct-graph
                    [{:file "DeSenseLight-0004A30B0019DD02.csv" :column 1}
                     {:file "DeSenseLight-0004A30B00199EB1.csv" :column 1}
                     ])
                  :humItems
                  (graph/construct-graph
                    [{:file "DeSenseSoil-0004A30B0019F784.csv" :column 1}
                     {:file "DeSenseSoil-0004A30B0019810D.csv" :column 1}
                     {:file "DeSense-0004A30B001A180C.csv" :column 2}
                     {:file "DeSense-0004A30B0019BE42.csv" :column 2}
                     {:file "DeSense-0004A30B00196841.csv" :column 2}
                     ])}))

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
           (GET "/cratechroom" [] (cratechroom-page))
           (GET "/greenhouse" [] (greenhouse-page))
           (GET "/about" [] (about-page)))
