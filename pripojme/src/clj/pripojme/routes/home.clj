(ns pripojme.routes.home
  (:require [pripojme.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn read-data []
  (let [data '({:x "2014-06-11 ", :y 10}
                {:x "2014-06-12 ", :y 25}
                {:x "2014-06-13 ", :y 30}
                {:x "2014-06-14 ", :y 10}
                {:x "2014-06-15 ", :y 15}
                {:x "2014-06-16 ", :y 30}
                )
        ]
    (str "[" (reduce str (interpose "," (map #(str "{x: \"" (:x %1) "\", y: " (:y %1) "}") data))) "]")
    )
  )

(defn about-page []
  (layout/render "about.html" {:cljItems (read-data)}))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))

