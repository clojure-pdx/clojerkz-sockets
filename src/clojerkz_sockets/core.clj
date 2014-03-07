(ns clojerkz-sockets.core
  (:require [clojure.core.async :refer [go chan map< put! >! <!]])
  (:import [java.net.ServerSocket]))

(defn start-server [& {:keys [port]}]
  (let [server-sock (java.net.ServerSocket. port)
        sock (future (.accept server-sock))
        in-stream (future (.getInputStream @sock))
        out-stream (future (.getOutputStream @sock))
        in (chan)]
    (println "listening on port 8080")
    (go
     (let [txt (slurp @in-stream)]
       (>! in txt)))
    {:in in
     :out out-stream
     :server server-sock
     :socket sock
     :shutdown (fn []
                 (.close @in-stream)
                 (.close @out-stream)
                 (.close @sock)
                 (.close server-sock))}))

(defn shutdown [{:keys [shutdown]}]
  (shutdown))

(defn echo [{:keys [in]}]
  (go
   (while true
     (let [msg (<! in)]
       (println (str "received message: " msg))))))
