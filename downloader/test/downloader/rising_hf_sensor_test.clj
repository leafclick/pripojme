(ns downloader.rising-hf-sensor-test
  (:require [clojure.test :refer :all]
            [downloader.rising-hf-sensor :refer :all]))

(deftest parse-temperature-test
  (testing "temperature above zero"
    (let [resp (parse-temperature "0150686f1e008919ca")]
      (is (= 24.750751953124997 resp))))
  (testing "temperature below zero"
    (let [resp (parse-temperature "01F83C6f1e008919ca")]
      (is (= -5.000668945312505 resp)))))

(deftest parse-humidity-test
  (testing "humidity"
    (let [resp (parse-humidity "0150686f1e008919ca")]
      (is (= 48.19921875 resp)))))

(deftest parse-period-test
  (testing "period"
    (let [resp (parse-period "0150686f1e008919ca")]
      (is (= 60 resp)))))

(deftest parse-rssi-test
  (testing "rssi"
    (let [resp (parse-rssi "0150686f1e008919ca")]
      (is (= -43 resp)))))

(deftest parse-snr-test
  (testing "positive snr"
    (let [resp (parse-snr "0150686f1e008919ca")]
      (is (= 6.25 resp))))
  (testing "negative snr"
    (let [resp (parse-snr "81F83C6f1e008919ca")]
      (is (= -57.75 resp)))))

(deftest parse-battery-test
  (testing "battery"
    (let [resp (parse-battery "0150686f1e008919ca")]
      (is (= 3.52 resp)))))

(deftest parse-all-test
  (testing "full parse test"
    (let [resp (parse-rising-hf "0150686f1e008919ca")]
      (is (= {:battery     3.52
              :humidity    48.19921875
              :period      60
              :rssi        -43
              :snr         6.25
              :temperature 24.750751953124997} resp))))
  (testing "negative snr"
    (let [resp (parse-rising-hf "811c686e1e009619cb")]
      (is (= {:battery     3.5300000000000002
              :humidity    47.7109375
              :period      60
              :rssi        -30
              :snr         -57.75
              :temperature 24.611325683593755} resp)))))