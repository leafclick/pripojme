(ns pripojme.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[pripojme started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[pripojme has shut down successfully]=-"))
   :middleware identity})
