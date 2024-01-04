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

(defn prepare-items-remaining-label [n]
  (if (> n 0)
    (str n (if (= n 1) " item " " items ") "remaining")
    ""))

(defn prepare-todo-item [{:keys [id title completed? editing?]}]
  {:class  (str (when completed? "completed ") (when editing? " editing"))
   :button {:class "destroy"
            :on-click [[:todo/delete-todo {:id id}]]}
   :toggle {:input
            {:type "checkbox"
             :class "toggle"
             :checked? completed?
             :on-click [[:todo/edit-todo {:id id :completed? (not completed?)}]]}}
   :input {:showing? editing?
           :value title
           :class "edit"
           :on-key-down [[:todo/edit-todo {:id id}]]}
   :label {:label title
           :on-dbl-click [[:todo/edit-todo {:id id :editing? true}]]}})

(defn prepare-todo-items [todos selected-filter]
  (mapv prepare-todo-item (filter (fn [todo]
                                    (or (= selected-filter :all)
                                        (and (= selected-filter :completed) (:completed? todo))
                                        (and (= selected-filter :active) (not (:completed? todo))))) todos)))

(defn prepare-ui-data [state]
  (let [selected-filter  (:filter state)
        todos            (:todos state)
        todos-remaining  (filter #(not (:completed? %)) todos)
        showing?         (> (count (:todos state)) 0)
        toggle-checked?  (= (count todos-remaining) 0)]
    {:list       {:items (prepare-todo-items todos selected-filter)
                  :showing? showing?}
     :header     {:heading "todos"}
     :input      {:type "text"
                  :value (:input state)
                  :class "new-todo"
                  :placeholder "What needs to be done?"
                  :on-key-down [[:todo/input]]}
     :toggle    {:showing? showing?
                 :input {:id "toggle-all"
                         :class "toggle-all"
                         :type "checkbox"
                         :checked? toggle-checked?
                         :on-click [[:todo/toggle-all {:toggle-to (not toggle-checked?)}]]}
                 :label  {:label-for "toggle-all"
                          :label "Mark all as complete"}}
     :footer     {:showing? showing?
                  :span {:label (prepare-items-remaining-label (count todos-remaining))
                         :class "todo-count"}
                  :filters [{:href "#/"
                             :label "All"
                             :class (when (= selected-filter :all) "selected")
                             :on-click [[:todo/select-filter {:filter :all}]]}
                            {:href "#/active"
                             :label "Active"
                             :class (when (= selected-filter :active) "selected")
                             :on-click [[:todo/select-filter {:filter :active}]]}
                            {:href "#/completed"
                             :label "Completed"
                             :class (when (= selected-filter :completed) "selected")
                             :on-click [[:todo/select-filter {:filter :completed}]]}]
                  :button {:showing? (boolean (some #(:completed? %) todos))
                           :class "clear-completed"
                           :on-click [[:todo/clear-completed]]
                           :label "Clear completed"}}}))

(defn handle-action [action payload state]
  (case action
    :todo/edit-todo (assoc state :todos
                           (mapv (fn [todo]
                                   (if (= (:id payload) (:id todo))
                                     (merge todo
                                            (select-keys
                                             payload [:id :title :completed? :editing?])
                                            (when
                                             (= return-key-code (get-in payload [:event :key-code]))
                                              {:title (or (get-in payload [:event :value]) "")
                                               :editing? false}))
                                     todo))
                                 (:todos state)))

    :todo/delete-todo (assoc state :todos
                             (filter (fn [todo] (not= (:id payload) (:id todo)))
                                     (:todos state)))

    :todo/toggle-all (let [toggle-to (:toggle-to payload)]
                       (assoc state :todos
                              (mapv (fn [todo] (assoc todo :completed? toggle-to))
                                    (:todos state))))

    :todo/clear-completed (assoc state :todos
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