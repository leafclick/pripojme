(ns downloader.solidus-tech-dth-test
  (:require [clojure.test :refer :all]
            [downloader.solidus-tech-dth :refer :all]))

(deftest parse-temperature-test
  (testing "temperature above zero"
    (let [resp (parse-temperature "0702281d1a28340a00")]
      (is (= 26.4 resp))))
  (testing "temperature below zero"
    (let [resp (parse-temperature "0702281d1a28340a01")]
      (is (= -26.4 resp)))))

(deftest parse-humidity-test
  (testing "humidity"
    (let [resp (parse-humidity "2408200315143C1E01")]
      (is (= 60.3 resp)))))

(deftest parse-snr-test
  (testing "positive snr"
    (let [resp (parse-snr "0702281d1a28340a00")]
      (is (= 2 resp))))
  (testing "negative snr"
    (let [resp (parse-snr "0f02281d1a28340a00")]
      (is (= -2 resp)))))

(deftest parse-battery-test
  (testing "battery"
    (let [resp (parse-battery "0702281d1a28340a00")]
      (is (= 4029 resp)))))

(deftest parse-all-test
  (testing "full parse test neg temp"
    (let [resp (parse-dth "2408200315143C1E01")]
      (is (= {:battery     3203
              :humidity    60.3
              :snr         8
              :temperature -21.2} resp))))
  (testing "full parse test pos temp"
    (let [resp (parse-dth "0702281d1a28340a00")]
      (is (= {:battery     4029
              :humidity    52.1
              :snr         2
              :temperature 26.4} resp)))))