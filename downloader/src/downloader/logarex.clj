(ns downloader.logarex)

(defn parse-hex-and-divide [hex]
  "parse hex value into int, divide by 100 and convert to double value"
  (double
    (/
      (Integer/parseInt hex 16)
      100
      )
    )
  )

(defn parse-active-consuption [payload]
  "active consuption in kWh"
  (let [hex (subs payload 2 10)]
    (parse-hex-and-divide hex)
    )
  )

(defn parse-active-distribution [payload]
  "active distribution in kWh"
  (let [hex (subs payload 10 18)]
    (parse-hex-and-divide hex)
    )
  )

(defn logarex-header []
  ["timestamp" "active-consuption" "active-distribution"])

(defn parse-logarex [payload]
  {:pre [(= (count payload) 18)]}
  {:active-consuption   (parse-active-consuption payload)
   :active-distribution (parse-active-distribution payload)
   }
  )