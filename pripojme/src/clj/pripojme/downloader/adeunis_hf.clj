(ns pripojme.downloader.adeunis-hf)

(defn parse-high-bits [hex]
  (unsigned-bit-shift-right (Integer/parseInt hex 16) 4)
  )

(defn parse-low-bits [hex]
  (bit-and (Integer/parseInt hex 16) 15)
  )

(defn parse-temperature [payload]
  (if (bit-test (Integer/parseInt (subs payload 0 2) 16) 7)
    (let [hex (subs payload 2 4)]
      (unchecked-byte (Integer/parseInt hex 16))
      )
    nil
    )
  )

(defn parse-rssi [payload]
  (if (bit-test (Integer/parseInt (subs payload 0 2) 16) 0)
    (let [len (count payload) hex (subs payload (- len 4) (- len 2))]
      (- (Integer/parseInt hex 16))
      )
    nil
    )
  )

(defn parse-snr [payload]
  (if (bit-test (Integer/parseInt (subs payload 0 2) 16) 0)
    (let [len (count payload) hex (subs payload (- len 2) len)]
      (unchecked-byte (Integer/parseInt hex 16))
      )
    nil
    )
  )

(defn parse-battery [payload]
  (let [info (Integer/parseInt (subs payload 0 2) 16) len (count payload)]
    (if (bit-test info 1)
      (if (bit-test info 0)
        (Integer/parseInt (subs payload (- len 8) (- len 4)) 16)
        (Integer/parseInt (subs payload (- len 4) len) 16)
        )
      nil
      )
    )
  )

(defn lat-hemisphere [hem]
  (if (bit-test hem 0)
    -1
    1)
  )

(defn parse-latitude [payload]
  (let [degHi (parse-high-bits (subs payload 0 2))
        degLo (parse-low-bits (subs payload 0 2))
        minHi (parse-high-bits (subs payload 2 4))
        minLo (parse-low-bits (subs payload 2 4))
        tenths (parse-high-bits (subs payload 4 6))
        hundredths (parse-low-bits (subs payload 4 6))
        thousandths (parse-high-bits (subs payload 6 8))
        hemisphere (parse-low-bits (subs payload 6 8))]
    (* (+
         (+ (* 10 degHi) degLo)
         (/ (+ (* 10 minHi) minLo (* 0.1 tenths) (* 0.01 hundredths) (* 0.001 thousandths)) 60)
         )
       (lat-hemisphere hemisphere)
       )
    )
  )

(defn long-hemisphere [hem]
  (if (bit-test hem 0)
    -1
    1)
  )

(defn parse-longtitude [payload]
  (let [degHu (parse-high-bits (subs payload 0 2))
        degHi (parse-low-bits (subs payload 0 2))
        degLo (parse-high-bits (subs payload 2 4))
        minHi (parse-low-bits (subs payload 2 4))
        minLo (parse-high-bits (subs payload 4 6))
        tenths (parse-low-bits (subs payload 4 6))
        hundredths (parse-high-bits (subs payload 6 8))
        hemisphere (parse-low-bits (subs payload 6 8))]
    (* (+
         (+ (* 100 degHu) (* 10 degHi) degLo)
         (/ (+ (* 10 minHi) minLo (* 0.1 tenths) (* 0.01 hundredths)) 60)
         )
       (long-hemisphere hemisphere)
       )
    )
  )

(defn parse-coordinates [payload]
  (let [info (Integer/parseInt (subs payload 0 2) 16)]
    (if (bit-test info 4)
      (if (bit-test info 7)
        (str "C(" (parse-latitude (subs payload 4 12)) "," (parse-longtitude (subs payload 12 20)) ")")
        (str "C(" (parse-latitude (subs payload 2 10)) "," (parse-longtitude (subs payload 10 18)) ")")
        )
      nil
      )
    )
  )

(defn adeunis-hf-header []
  ["timestamp" "temperature" "coordinates" "battery" "rssi" "snr"])

(defn parse-adeunis-hf [payload]
  {:pre [(> (count payload) 2)]}
  {:temperature (parse-temperature payload)
   :coordinates (parse-coordinates payload)
   :battery     (parse-battery payload)
   :rssi        (parse-rssi payload)
   :snr         (parse-snr payload)
   }
  )