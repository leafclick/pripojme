(ns downloader.develict-desense)

(defn parse-hex-and-divide [hex]
  "parse hex value into int, divide by 1000 and convert to double value"
  (double
    (/
      (Integer/parseInt hex 16)
      1000
      )
    )
  )

(defn parse-temperature [payload]
  (let [hex (subs payload 10 14)]
    (parse-hex-and-divide hex)
    )
  )

(defn parse-humidity [payload]
  (let [hex (subs payload 14 18)]
    (parse-hex-and-divide hex)
    )
  )

(defn parse-light [payload]
  "Light in lx"
  (let [hex (subs payload 10 14)]
    (Integer/parseInt hex 16)
    )
  )

; deleni 100 pridano na zaklade dat a odporuje manualu
(defn parse-moisture [payload]
  "Moisture in %"
  (let [hex (subs payload 10 14)]
    (double
      (/
        (Integer/parseInt hex 16)
        100
        )
      )
    )
  )
(defn parse-noise [payload]
  "Noise in db"
  (let [hex (subs payload 10 14)]
    (double
      (/
        (Integer/parseInt hex 16)
        100
        )
      )
    )
  )

(defn parse-wind-velocity [payload]
  "wind radial velocity"
  (let [hex (subs payload 10 14)]
    (parse-hex-and-divide hex)
    )
  )

(defn parse-wind-temperature [payload]
  (let [hex (subs payload 14 18)]
    (parse-hex-and-divide hex)
    )
  )

(defn parse-rssi [payload]
  "RSSI in dBm"
  (let [hex (subs payload 2 4)]
    (unchecked-byte (Integer/parseInt hex 16))
    )
  )

(defn parse-snr [payload]
  "SNR in dB"
  (let [hex (subs payload 4 6)]
    (unchecked-byte (Integer/parseInt hex 16))
    )
  )

(defn parse-battery [payload]
  "Battery voltage in mV"
  (let [hex (subs payload 6 10)]
    (Integer/parseInt hex 16)
    )
  )

(defn desens-header []
  ["timestamp" "temperature" "humidity" "rssi" "snr" "battery"])

(defn desens-light-header []
  ["timestamp" "light" "rssi" "snr" "battery"])

(defn desens-soil-header []
  ["timestamp" "moisture" "rssi" "snr" "battery"])

(defn desens-noise-header []
  ["timestamp" "noise" "rssi" "snr" "battery"])

(defn desens-wind-header []
  ["timestamp" "velocity" "temperature" "rssi" "snr" "battery"])

(defn parse-desens [payload]
  {:pre [(= (count payload) 18)]}
  {:temperature (parse-temperature payload)
   :humidity    (parse-humidity payload)
   :rssi        (parse-rssi payload)
   :snr         (parse-snr payload)
   :battery     (parse-battery payload)
   }
  )

(defn parse-desens-light [payload]
  {:pre [(= (count payload) 14)]}
  {:light   (parse-light payload)
   :rssi    (parse-rssi payload)
   :snr     (parse-snr payload)
   :battery (parse-battery payload)
   }
  )

(defn parse-desens-soil [payload]
  {:pre [(= (count payload) 14)]}
  {:moisture (parse-moisture payload)
   :rssi     (parse-rssi payload)
   :snr      (parse-snr payload)
   :battery  (parse-battery payload)
   }
  )

(defn parse-desens-noise [payload]
  {:pre [(= (count payload) 14)]}
  {:noise   (parse-noise payload)
   :rssi    (parse-rssi payload)
   :snr     (parse-snr payload)
   :battery (parse-battery payload)
   }
  )

(defn parse-desens-wind [payload]
  {:pre [(= (count payload) 18)]}
  {
   :velocity    (parse-wind-velocity payload)
   :temperature (parse-wind-temperature payload)
   :rssi        (parse-rssi payload)
   :snr         (parse-snr payload)
   :battery     (parse-battery payload)
   }
  )