(ns downloader.logarex-test
  (:require [clojure.test :refer :all]
            [downloader.logarex :refer :all]))

(deftest parse-active-consuption-test
  (testing "consumprion"
    (let [resp (parse-active-consuption "210000888900000000")]
      (is (= 349.53 resp)))))

(deftest parse-active-distribution-test
  (testing "distribution"
    (let [resp (parse-active-distribution "210000000000001140")]
      (is (= 44.16 resp)))))

(deftest parse-all-test
  (testing "full parse test"
    (let [resp (parse-logarex "210000889700001140")]
      (is (= {:active-consuption   349.67
              :active-distribution 44.16} resp))))
  )