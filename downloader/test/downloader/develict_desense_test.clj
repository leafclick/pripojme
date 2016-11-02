(ns downloader.develict-desense-test
  (:require [clojure.test :refer :all]
            [downloader.develict-desense :refer :all]))

(deftest parse-temperature-test
  (testing "temperature above zero"
    (let [resp (parse-temperature "03ffff0e0a46993365")]
      (is (= 18.073 resp))))
  ;(testing "temperature below zero"
  ;  (let [resp (parse-temperature "")]
  ;    (is (= -5.000668945312505 resp))))
  )

(deftest parse-humidity-test
  (testing "humidity"
    (let [resp (parse-humidity "03ffff0e0a46993365")]
      (is (= 13.157 resp)))))

(deftest parse-rssi-test
  (testing "rssi"
    (let [resp (parse-rssi "03ffff0e0a46993365")]
      (is (= -1 resp)))))

(deftest parse-snr-test
  ;(testing "positive snr"
  ;  (let [resp (parse-snr "")]
  ;    (is (= 25/4 resp))))
  (testing "negative snr"
    (let [resp (parse-snr "03ffff0e0a46993365")]
      (is (= -1 resp)))))

(deftest parse-battery-test
  (testing "battery"
    (let [resp (parse-battery "03ffff0e0a46993365")]
      (is (= 3594 resp)))))

(deftest parse-all-test
  (testing "full parse test"
    (let [resp (parse-desens "03ffff0e0a46993365")]
      (is (= {:battery     3594
              :humidity    13.157
              :rssi        -1
              :snr         -1
              :temperature 18.073} resp))))
  )

(deftest parse-soil-test
  (testing "full parse test"
    (let [resp (parse-desens-soil "05ff860d590ab7")]
      (is (= {:battery  3417
              :moisture 27.43
              :rssi     -1
              :snr      -122} resp))))
  )

(deftest parse-light-test
  (testing "full parse test"
    (let [resp (parse-desens-light "04ff870d7a0b35")]
      (is (= {:battery 3450
              :light   2869
              :rssi    -1
              :snr     -121} resp))))
  )

(deftest parse-wind-test
  (testing "full parse test"
    (let [resp (parse-desens-wind "08ff880c78075008c9")]
      (is (= {:battery     3192
              :rssi        -1
              :snr         -120
              :temperature 2.249
              :velocity    1.872} resp))))
  )