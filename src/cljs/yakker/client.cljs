(ns yakker.client
  (:require [cognitect.transit :as t]
            [reagent.core :as reagent]))

;;; Lifted from:
;;; https://yogthos.net/posts/2015-06-11-Websockets.html

;;; Transit over Websockets
(defonce ws-chan (reagent/atom nil))
(defonce messages (reagent/atom []))

(def ^:private json-reader (t/reader :json))
(def ^:private json-writer (t/writer :json))

(defn receive-transit-msg! [update-fn]
  (fn [msg]
    (update-fn (->> msg .-data (t/read json-reader)))))

(defn send-transit-msg! [msg]
  (if @ws-chan
    (.send @ws-chan (t/write json-writer msg))
    (throw (js/Error. "Websocket is not available!"))))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect websocket")
  (if-let [chan (js/WebSocket. url)]
    (do (set! (.-onmessage chan) (receive-transit-msg! receive-handler))
        (reset! ws-chan chan)
        (println "Websocket connection established with: " url))
    (throw (js/Error. "Websocket connection failed!"))))

(defn update-messages! [{:keys [message]}]
  (swap! messages #(vec (take 10 (conj % message)))))
