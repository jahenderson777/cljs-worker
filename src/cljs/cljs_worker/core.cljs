(ns cljs-worker.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [cljs-worker.events :as events]
   [cljs-worker.views :as views]
   [cljs-worker.config :as config]
   ))

(def workers (atom []))

(def memory (js/WebAssembly.Memory. (clj->js {:initial 80
                                              :maximum 80
                                              :shared true})))

(def config (clj->js {:x -0.743644786
                      :y 0.1318252536
                      :d 0.00029336}))

(def worker-count 2)
(def done-count (atom 0))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (doseq [i (range worker-count)]
    (let [worker (js/Worker. "worker.js")]
      (swap! workers conj worker)
      (set! (.-onmessage worker)
            (fn [evt]
              (println "onmessage2" i (.-data evt))
              (swap! done-count inc)
              (when (= @done-count worker-count)
                (let [canvas-data (js/Uint8Array. (.-buffer memory)
                                                  4
                                                  1200 * 800 * 4)
                      ctx (-> (js/document.querySelector "canvas")
                              (.getContext "2d"))
                      _ (do (.beginPath ctx)
                            (.moveTo ctx 0 0)
                            (.lineTo ctx 300 140)
                            (.stroke ctx))
                      image-data-test (.getImageData ctx 0 0 1200 800)
                      image-data (.createImageData ctx 1200 800)]
                  
                  (.set (.-data image-data) canvas-data)
                  ;(println (.-data image-data))
                  (.putImageData ctx image-data 1 90 0 0 1200 800)
                  
                  (println "done here" i)))))
      (.postMessage worker (clj->js {:memory memory
                                     :config config
                                     :id (+ 100 (* i 200))}))))
  (mount-root))
