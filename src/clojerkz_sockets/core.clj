(ns clojerkz-sockets.core
  (:require [clojure.core.async :refer [go chan map< put! >! <!]]
            [clojure.java.io :as io])
  (:import [java.net.ServerSocket]))

(defn start-server [& {:keys [port]}]
  (let [server-sock (java.net.ServerSocket. port)
        sock (future (.accept server-sock))
        in-stream (future (.getInputStream @sock))
        out-stream (future (.getOutputStream @sock))
        in (chan)
        out (chan)]
    (println "listening on port 8080")
    (go
     (let [reader (io/reader @in-stream)]
       (while true
         (let [msg (.readLine reader)]
           (>! in msg)))))
    (go
     (let [writer (io/writer @out-stream)]
       (while true
         (let [msg (<! out)]
           (.write writer msg)
           (.flush writer)))))
    {:in in
     :out out
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
