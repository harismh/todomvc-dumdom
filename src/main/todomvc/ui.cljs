(ns todomvc.ui
  (:require [dumdom.core :refer [defcomponent]]))

(defcomponent input [{:keys [value]}]
  [:input.new-todo {:type "text"
                    :value value
                    :placeholder "What needs to be done?"
                    :on-key-down [[:todo/input]]}])

(defcomponent header [data]
  [:header.header
   [:h1 "todos"]
   [input data]])

(defcomponent toggle-all []
  [:span
   [:input#toggle-all.toggle-all {:type "checkbox"
                                  :on-click [[:todo/toggle-all]]}]
   [:label {:for "toggle-all"} "Mark all as complete"]])

(defcomponent filter-toggle [{:keys [href, selected?, label, filter]}]
  [:a {:class (when selected? "selected")
       :href href
       :on-click [[:todo/select-filter {:filter filter}]]} label])

(defcomponent footer-filters [selected]
  [:ul.filters
   [:li [filter-toggle {:href "#/" :label "All" :selected? (= selected :all) :filter :all}]]
   [:li [filter-toggle {:href "#/active" :label "Active" :selected? (= selected :active) :filter :active}]]
   [:li [filter-toggle {:href "#/completed" :label "Completed" :selected? (= selected :completed) :filter :completed}]]])

(defcomponent footer-count [label]
  [:span.todo-count label])

(defcomponent footer-clear-completed []
  [:button.clear-completed {:on-click [[:todo/clear-completed]]}
   "Clear completed"])

(defn external-link [href text]
  [:a {:href href :target "_blank" :rel "noopener noreferrer"} text])

(defcomponent footer [data]
  [:footer.footer
   [footer-count (:remaining-todos-label data)]
   [footer-filters (:selected-filter data)]
   (when (:showing-clear-complete? data)
     [footer-clear-completed])
   [:footer.info
    [:p "Double-click to edit a todo."]
    [:p "Code by "
     (external-link "https://github.com/harismh/todomvc_dumdom" "harismh.")]
    [:p "Styles sourced from "
     (external-link "https://todomvc.com" "TodoMVC.")]]])


(defcomponent todo-checkbox [{:keys [id completed?]}]
  [:input.toggle {:type "checkbox"
                  :checked completed?
                  :on-change [[:todo/edit-todo {:id id :completed? (not completed?)}]]}])

(defcomponent todo-item [{:keys [title id completed? editing?] :as todo}]
  [:li {:class (str (when completed? "completed ") (when editing? " editing"))}
   [:div.view
    (todo-checkbox todo)
    [:label {:on-dbl-click [[:todo/edit-todo {:id id :editing? true}]]} title]
    [:button.destroy {:on-click [[:todo/delete-todo {:id id}]]}]]
   (when editing? [:input.edit {:type "text"
                                :value title
                                :on-key-down [[:todo/edit-todo {:id id}]]}])])

(defcomponent shell [data]
  (let [list-data   (:list data)
        toggle-data (:toggle-all data)
        footer-data (:footer data)
        input-data  (:input data)]
    [:main
     [:section.todoapp
      [header (:value input-data)]
      [:section
       [:section.main
        (when (:showing? toggle-data)
          [:span
           [toggle-all]
           [:ul.todo-list
            (for [todo (:todos list-data)]
              [todo-item todo])]])]
       (when (:showing? footer-data)
         [footer footer-data])]]]))