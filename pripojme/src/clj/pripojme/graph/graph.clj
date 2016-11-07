(ns pripojme.graph.graph)

(defn read-data []
  (let [data '({:x "2014-06-11 ", :y 10}
                {:x "2014-06-12 ", :y 25}
                {:x "2014-06-13 ", :y 30}
                {:x "2014-06-14 ", :y 10}
                {:x "2014-06-15 ", :y 15}
                {:x "2014-06-16 ", :y 30}
                )
        ]
    (interpose "," data)
    )
  )