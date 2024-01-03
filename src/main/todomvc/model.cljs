(ns todomvc.model)

(def return-key-code 13)

(def available-actions
  #{:todo/edit-todo
    :todo/delete-todo
    :todo/toggle-all
    :todo/select-filter
    :todo/clear-completed
    :todo/input})

(def initial-state
  {:todos []
   :input ""
   :filter :all})

(defn prepare-todos-label [todos-count]
  (if (> todos-count 0)
    (str todos-count (if (= todos-count 1) " item " " items ") "remaining")
    ""))

(defn prepare-todo [m]
  (select-keys m [:id :title :completed? :editing?]))

(defn prepare-todos-list [todos selected-filter]
  (->> (mapv prepare-todo todos)
       (filter (fn [todo] (or (= selected-filter :all)
                              (and (= selected-filter :completed) (:completed? todo))
                              (and (= selected-filter :active) (not (:completed? todo))))))))

(defn prepare-ui-data [state]
  (let [todos           (prepare-todos-list (:todos state) (:filter state))
        has-todos?      (> (count (:todos state)) 0)
        remaining-todos (filter #(not (:completed? %)) todos)
        remaining-todos-count (count remaining-todos)]
    {:list       {:todos todos
                  :showing? has-todos?}
     :input      {:value (:input state)}
     :toggle-all {:showing? has-todos?}
     :footer     {:remaining-todos-label
                  (prepare-todos-label remaining-todos-count)
                  :remaining-todos-count remaining-todos-count
                  :showing? has-todos?
                  :selected-filter (:filter state)
                  :showing-clear-complete? (boolean (some #(:completed? %) todos))}}))

(defn handle-action [action payload state]
  (case action
    :todo/edit-todo (assoc-in state [:todos]
                              (mapv (fn [todo]
                                      (if (= (:id payload) (:id todo))
                                        (merge todo
                                               (prepare-todo payload)
                                               (when
                                                (= return-key-code (get-in payload [:event :key-code]))
                                                 {:title (or (get-in payload [:event :value]) "")
                                                  :editing? false}))
                                        todo))
                                    (:todos state)))

    :todo/delete-todo (assoc-in state [:todos]
                                (filter (fn [todo] (not= (:id payload) (:id todo)))
                                        (:todos state)))

    :todo/toggle-all (let [toggled (if (true? (get-in payload [:event :checked?]))
                                     false
                                     true)]
                       (assoc-in state [:todos]
                                 (mapv (fn [todo] (assoc todo :completed? toggled))
                                       (:todos state))))

    :todo/clear-completed (assoc-in state [:todos]
                                    (filter (fn [todo] (not (:completed? todo)))
                                            (:todos state)))

    :todo/select-filter (assoc state :filter (:filter payload))

    :todo/input (let [input          (get-in payload [:event :value])
                      is-return-key? (= return-key-code (get-in payload [:event :key-code]))]
                  (if (and (not= "" input) is-return-key?)
                    (-> state
                        (assoc :input "")
                        (assoc :todos (into [{:id (random-uuid)
                                              :title input
                                              :completed? false
                                              :editing? false}]
                                            (:todos state))))
                    (assoc state :input input)))
    state))

(comment
  (prepare-ui-data
   (merge initial-state
          {:todos [{:id "v3pYXsm0"
                    :title "Complete me"
                    :completed? false
                    :editing? false}
                   {:id "CaLZgRDx"
                    :title "Completed me"
                    :completed? true
                    :editing? false}
                   {:id "1yg10NhY"
                    :title "Editing me"
                    :completed? false
                    :editing? true}]})))