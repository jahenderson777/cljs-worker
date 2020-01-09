(ns cljs-worker.views
  (:require
   [re-frame.core :as re-frame]
   [cljs-worker.subs :as subs]
   ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]
     ]))
