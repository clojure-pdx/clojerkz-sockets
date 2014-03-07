(ns clojerkz-sockets.core
  (:require [clojure.core.async :refer [go chan map< put! >! <! <!!]]
            [clojure.java.io :as io])
  (:import [java.net.ServerSocket]))

(defn socket-connection [sock]
  (let [in-stream (.getInputStream sock)
        out-stream (.getOutputStream sock)
        reader (io/reader in-stream)
        writer (io/writer out-stream)
        in (chan)
        out (chan)]
    (go (while true
          (let [msg (.readLine reader)]
            (>! in msg))))
    (go (while true
          (let [msg (<! out)]
            (.write writer msg)
            (.flush writer))))
    {:in in
     :out out
     :socket sock
     :shutdown (fn []
                 (.close in-stream)
                 (.close out-stream)
                 (.close sock))}))

(defn start-server [& {:keys [port]}]
  (let [server-sock (java.net.ServerSocket. port)
        connections (chan)]

    (go
     (while true
       (let [connection (.accept server-sock)]
         (println "received connection")
         (put! connections (socket-connection connection)))))

    (println "listening on port" port)

    connections))

(defn relay [sock1 sock2]
  (go
   (while true
     (let [msg (<! (:in sock1))]
       (println "received message:" msg)
       (put! (:out sock2) msg)))))

(defn shutdown [{:keys [shutdown]}]
  (shutdown))

(defn echo [{:keys [in out]}]
  (go
   (while true
     (let [msg (<! in)]
       (println (str "received message: " msg))
       (put! out (str "you said \"" msg "\"\n"))))))

(defn echo-connections [port]
  (let [connections (start-server :port port)]
    (go (while true
      (let [conn (<! connections)]
        (echo conn))))))

(defn -main [& args]
  (<!! (echo-connections 8080)))
