(ns pripojme.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [pripojme.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[pripojme started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[pripojme has shut down successfully]=-"))
   :middleware wrap-dev})
