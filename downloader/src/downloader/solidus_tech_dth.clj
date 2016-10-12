(ns downloader.solidus-tech-dth)

(defn parse-snr [payload]
  "Pokud je v hlavicce nastaven bit D3, snr je zaporne"
  (let [hex (subs payload 2 4) header (subs payload 0 2)]
    (if (bit-test (Integer/parseInt header 16) 3)
      (- (Integer/parseInt hex 16))
      (Integer/parseInt hex 16)
      )
    )
  )

(defn parse-battery [payload]
  "Battery voltage in mV"
  (let [batHi (subs payload 4 6) batLo (subs payload 6 8)]
    (+
      (* 100 (Integer/parseInt batHi 16))
      (Integer/parseInt batLo 16)
      )
    )
  )

(defn parse-absolute-temperature [payload]
  (let [tempHi (subs payload 8 10) tempLo (subs payload 10 12)]
    (+
      (Integer/parseInt tempHi 16)
      (* 0.01 (Integer/parseInt tempLo 16))
      )
    )
  )

(defn parse-temperature [payload]
  "Temperature in °C"
  (let [suppByte (subs payload 16 18) absTemp (parse-absolute-temperature payload)]
    (if (bit-test (Integer/parseInt suppByte 16) 0)
      (- absTemp)
      absTemp
      )
    )
  )

(defn parse-humidity [payload]
  "Humidity in %"
  (let [humHi (subs payload 12 14) humLo (subs payload 14 16)]
    (+
      (Integer/parseInt humHi 16)
      (* 0.01 (Integer/parseInt humLo 16))
      )
    )
  )

(defn dth-header []
  ["timestamp" "temperature" "humidity" "snr" "battery"])

(defn parse-dth [payload]
  {:pre [(= (count payload) 18)]}
  {:temperature (parse-temperature payload)
   :humidity    (parse-humidity payload)
   :snr         (parse-snr payload)
   :battery     (parse-battery payload)
   }
  )