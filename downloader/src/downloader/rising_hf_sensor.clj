(ns downloader.rising-hf-sensor)

(defn parse-temperature [payload]
  (let [hex (str (subs payload 4 6) (subs payload 2 4))]
    (double
      (- (/
           (* 175.72 (Integer/parseInt hex 16))
           (Math/pow 2 16))
         46.85)
      )
    )
  )

(defn parse-humidity [payload]
  (let [hex (subs payload 6 8)]
    (double
      (-
        (/
          (* 125 (Integer/parseInt hex 16))
          (Math/pow 2 8))
        6)
      )
    )
  )

(defn parse-period [payload]
  (let [hex (str (subs payload 10 12) (subs payload 8 10))]
    (* 2 (Integer/parseInt hex 16))
    )
  )

(defn parse-rssi [payload]
  (let [hex (subs payload 12 14)]
    (+ -180 (Integer/parseInt hex 16))
    )
  )

(defn parse-snr [payload]
  "Pokud je v hlavicce nastaven bit D7, snr je zaporne"
  (let [hex (subs payload 14 16) header (subs payload 0 2)]
    (double
      (/
        (if (bit-test (Integer/parseInt header 16) 7)
          (- (Integer/parseInt hex 16) 256)
          (Integer/parseInt hex 16)
          )
        4
        )
      )
    )
  )

(defn parse-battery [payload]
  (let [hex (subs payload 16 18)]
    (*
      (+ 150 (Integer/parseInt hex 16))
      0.01
      )
    )
  )

(defn rhf-header []
  ["timestamp" "temperature" "humidity" "period" "rssi" "snr" "battery"])

(defn parse-rising-hf [payload]
  {:pre [(= (count payload) 18)]}
  {:temperature (parse-temperature payload)
   :humidity    (parse-humidity payload)
   :period      (parse-period payload)
   :rssi        (parse-rssi payload)
   :snr         (parse-snr payload)
   :battery     (parse-battery payload)
   }
  )

