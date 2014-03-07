(ns clojerkz-sockets.core
  (:import [java.net.ServerSocket]))

(defn start-server [& {:keys [port]}]
  (let [server-sock (java.net.ServerSocket. port)
        sock (.accept server-sock)
        in-stream (.getInputStream sock)
        out-stream (.getOutputStream sock)]
    (println "listening on port 8080")
    {:in in-stream
     :out out-stream
     :server server-sock
     :socket sock}))
