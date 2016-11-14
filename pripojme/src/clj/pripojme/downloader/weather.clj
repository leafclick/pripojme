(ns pripojme.downloader.weather)

(defn parse-weather-condition
  "returns human-readable representation of skyspark weatherCondition value"
  [condition]
  (case condition
    0 "neznámé"
    1 "jasno"
    2 "polojasno"
    3 "zataženo"
    4 "přeháňky"
    5 "déšť"
    6 "bouřky"
    7 "ledovka"
    8 "sněhové přeháňky"
    9 "sněžení"
    )
  )
