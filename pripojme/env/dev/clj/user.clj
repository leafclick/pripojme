(ns user
  (:require [mount.core :as mount]
            pripojme.core))

(defn start []
  (mount/start-without #'pripojme.core/repl-server))

(defn stop []
  (mount/stop-except #'pripojme.core/repl-server))

(defn restart []
  (stop)
  (start))


