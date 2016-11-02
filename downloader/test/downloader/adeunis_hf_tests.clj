(ns downloader.adeunis-hf-tests
  (:require [clojure.test :refer :all]
            [downloader.adeunis-hf :refer :all]))

(deftest parse-all-test
  (testing "full parse test"
    (let [resp (parse-adeunis-hf "9f1850060710016210405d420cfd86f4")]
      (is (= {:battery     3325
              :coordinates "C(50.10118333333333,16.350666666666665)"
              :rssi        -134
              :snr         -12
              :temperature 24} resp))))
  (testing "battery, coordinates"
    (let [resp (parse-adeunis-hf "1e50060710016210405d420cfd")]
      (is (= {:battery     3325
              :coordinates "C(50.10118333333333,16.350666666666665)"
              :rssi        nil
              :snr         nil
              :temperature nil} resp)))))