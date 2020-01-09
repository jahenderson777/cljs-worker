(ns cljs-worker.events
  (:require
   [re-frame.core :as re-frame]
   [cljs-worker.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
