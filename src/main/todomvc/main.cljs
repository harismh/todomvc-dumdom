(ns todomvc.main
  (:require
   [cljs.reader :as reader]
   [dumdom.core :as dd]
   [todomvc.ui :as ui]
   [todomvc.model :as model]))

(def ^:constant storage-key "todomvc-dumdom")

(defonce store (atom model/initial-state))

(defn render! []
  (dd/render
   [ui/shell (model/prepare-ui-data @store)]
   (js/document.getElementById "app")))

(defn restore-state! []
  (let [filter-hash (->> (str
                          (.-hash js/window.location))
                         (re-find #"[^#/]*$"))
        filter (or (when (not-empty filter-hash)
                     (keyword filter-hash))
                   :all)
        state (or
               (reader/read-string (.getItem (.-localStorage js/window) storage-key))
               model/initial-state)]
    (reset! store (assoc state :filter filter))))

(defn save-state! [key]
  (.setItem (.-localStorage js/window) key @store))

(dd/set-event-handler!
 (fn [event actions]
   (doseq [[action data] actions]
     (if (nil? (action model/available-actions))
       (throw (js/Error. (str "Unknown action " action)))
       (let [event-val (.. event -target -value)
             event-chk (.. event -target -checked)
             event-key (.. event -keyCode)
             payload   (cond-> (or data {})
                         event-val (assoc-in [:event :value] event-val)
                         event-key (assoc-in [:event :key-code] event-key)
                         event-chk (assoc-in [:event :checked?] event-chk))]
         (prn "Triggered action" action payload)
         (reset! store (model/handle-action action payload @store)))))))

(add-watch store ::re-render (fn [_ _ _ _] (render!)))

(add-watch store ::save-state (fn [_ _ _ _] (save-state! storage-key)))

(defn ^:export init []
  (restore-state!)
  (render!))

(comment
  @store)