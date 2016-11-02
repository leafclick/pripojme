(ns downloader.dispatch
  (:require [downloader.solidus-tech-dth :as dth]
            [downloader.rising-hf-sensor :as rhf]
            [downloader.logarex :as lgx]
            [downloader.adeunis-hf :as ade]
            [downloader.develict-desense :as des]))

(defmulti get-column-names (fn [x] x))

(defmethod get-column-names "DTH" [_]
  (dth/dth-header))

(defmethod get-column-names "ARF8084BA" [_]
  (ade/adeunis-hf-header))

(defmethod get-column-names "DeSense" [_]
  (des/desens-header))

(defmethod get-column-names "DeSenseWind" [_]
  (des/desens-wind-header))

(defmethod get-column-names "DeSenseSoil" [_]
  (des/desens-soil-header))

(defmethod get-column-names "DeSenseNoise" [_]
  (des/desens-noise-header))

(defmethod get-column-names "DeSenseLight" [_]
  (des/desens-light-header))

(defmethod get-column-names "EL001" [_]
  (lgx/logarex-header))

(defmethod get-column-names "RHF1S001" [_]
  (rhf/rhf-header))

(defmethod get-column-names :default [x]
  (throw (IllegalArgumentException.
           (str "Unknown model " x))))


(defmulti parse-payload (fn [x _] x))

(defmethod parse-payload "DTH" [_ payload]
  (dth/parse-dth payload))

(defmethod parse-payload "ARF8084BA" [_ payload]
  (ade/parse-adeunis-hf payload))

(defmethod parse-payload "DeSense" [_ payload]
  (des/parse-desens payload))

(defmethod parse-payload "DeSenseWind" [_ payload]
  (des/parse-desens-wind payload))

(defmethod parse-payload "DeSenseSoil" [_ payload]
  (des/parse-desens-soil payload))

(defmethod parse-payload "DeSenseNoise" [_ payload]
  (des/parse-desens-noise payload))

(defmethod parse-payload "DeSenseLight" [_ payload]
  (des/parse-desens-light payload))

(defmethod parse-payload "EL001" [_ payload]
  (lgx/parse-logarex payload))

(defmethod parse-payload "RHF1S001" [_ payload]
  (rhf/parse-rising-hf payload))

(defmethod parse-payload :default [x _]
  (throw (IllegalArgumentException.
           (str "Unknown model " x))))