(ns pripojme.graph.devices)


(def devices-cratechroom
  [{:devEUI      "0018B20000066679",
    :graphId     0,
    :projectId   "CRaTechnologyRoom",
    :description "Teplotni a vlhkostni cidlo",
    :model       "DTH",
    :vendor      "Solidus Tech"}
   {:devEUI      "0018B20000066681",
    :graphId     1,
    :projectId   "CRaTechnologyRoom",
    :description "Teplotni a vlhkostni cidlo",
    :model       "DTH",
    :vendor      "Solidus Tech"}
   {:devEUI      "prague-2016",
    :graphId     2,
    :projectId   "Weather",
    :description "Teplotni udaje Praha 2016",
    :model       "weather",
    :vendor      "NA"}]
  )

(def cratechroom-temp-data
  [{:devEUI "0018B20000066679" :column 1}
   {:devEUI "0018B20000066681" :column 1}
   {:devEUI "prague-2016" :column 1}]
  )

(def devices-greenhouse
  [{:devEUI      "0004A30B0019BE42",
    :graphId     0,
    :projectId   "Greenhouse20",
    :description "Teplotni a vlhkostni cidlo (vzduch)",
    :model       "DeSense",
    :vendor      "Develict"}
   {:devEUI      "0004A30B001A180C",
    :graphId     1,
    :projectId   "Greenhouse20",
    :description "Teplotni a vlhkostni cidlo (vzduch)",
    :model       "DeSense",
    :vendor      "Develict"}
   {:devEUI      "0004A30B00196841",
    :graphId     2,
    :projectId   "Greenhouse20",
    :description "Teplotni a vlhkostni cidlo (vzduch)",
    :model       "DeSense",
    :vendor      "Develict"}
   {:devEUI      "0004A30B0019810D",
    :graphId     3,
    :projectId   "Greenhouse20",
    :description "Cidlo vodivosti pudy (zadni cast skleniku)",
    :model       "DeSenseSoil",
    :vendor      "Develict"}
   {:devEUI      "0004A30B0019F784",
    :graphId     4,
    :projectId   "Greenhouse20",
    :description "Cidlo vodivosti pudy (predni cast skleniku)",
    :model       "DeSenseSoil",
    :vendor      "Develict"}
   {:devEUI      "0004A30B0019DD02",
    :graphId     5,
    :projectId   "Greenhouse20",
    :description "Cidlo intenzity svetla (zadni cast skleniku)",
    :model       "DeSenseLight",
    :vendor      "Develict"}
   {:devEUI      "0004A30B00199EB1",
    :graphId     6,
    :projectId   "Greenhouse20",
    :description "Cidlo intenzity svetla (predni cast skleniku)",
    :model       "DeSenseLight",
    :vendor      "Develict"}
   {:devEUI      "prague-2016",
    :graphId     7,
    :projectId   "weather",
    :description "Teplotni udaje Praha 2016",
    :model       "weather",
    :vendor      "NA"}]
  )

(def greenhouse-temp-data
  [{:devEUI "0004A30B001A180C" :column 1}
   {:devEUI "0004A30B0019BE42" :column 1}
   {:devEUI "0004A30B00196841" :column 1}
   {:devEUI "prague-2016" :column 1}]
  )

(def greenhouse-light-data
  [{:devEUI "0004A30B0019DD02" :column 1}
   {:devEUI "0004A30B00199EB1" :column 1}]
  )

(def greenhouse-hum-data
  [{:devEUI "0004A30B0019F784" :column 1}
   {:devEUI "0004A30B0019810D" :column 1}
   {:devEUI "0004A30B001A180C" :column 2}
   {:devEUI "0004A30B0019BE42" :column 2}
   {:devEUI "0004A30B00196841" :column 2}]
  )


(defn check-device [checked-devices device]
  (if (some (fn [name] (= (:devEUI device) name)) checked-devices)
    (conj device {:checked "checked"})
    device
    )
  )

(defn check-devices [checked-devices devices]
  (map #(check-device checked-devices %1) devices)
  )



(defn add-groups-to-devices [devices data-sources]
  (map #(merge % (first (filter (fn [device] (= (:devEUI %) (:devEUI device))) devices))) data-sources)
  )

(defn filter-checked-devices [checked-devices possible-devices]
  (filter #(some (fn [devEUI] (.equals (%1 :devEUI) devEUI)) checked-devices) possible-devices)
  )