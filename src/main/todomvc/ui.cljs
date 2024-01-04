(ns todomvc.ui
  (:require [dumdom.core :refer [defcomponent]]))

;;;; Building Blocks

(defcomponent input [{:keys [id, type, value, placeholder, class,
                             checked?, on-click, on-key-down]}]
  [:input {:id id
           :type (or type "text")
           :value value
           :placeholder placeholder
           :checked checked?
           :class class
           :on-click on-click
           :on-key-down on-key-down}])

(defcomponent anchor [{:keys [href, label,
                              on-click, class,
                              rel, target]}]
  [:a {:class class
       :href href
       :on-click on-click
       :target target
       :rel rel}
   label])

(defcomponent span [{:keys [class label]}]
  [:span {:class class}
   label])

(defcomponent button [{:keys [class on-click label]}]
  [:button {:class class
            :on-click on-click}
   label])

(defcomponent label [{:keys [label label-for on-dbl-click]}]
  [:label {:for label-for
           :on-dbl-click on-dbl-click}
   label])

;;;; Composed Blocks

(def footer-notice
  [:footer.info
   [:p "Double-click to edit a todo."]
   [:p "Code by "
    [anchor {:href "https://github.com/harismh/todomvc-dumdom"
             :label "harismh."
             :target "_blank"
             :rel "noopener noreferrer"}]]
   [:p "Styles sourced from "
    [anchor {:href "https://todomvc.com"
             :label "TodoMVC."
             :target "_blank"
             :rel "noopener noreferrer"}]]])

(defcomponent header [data]
  [:header.header
   [:h1 (:heading (:header data))]
   [input (:input data)]])

(defcomponent footer [footer-data]
  [:footer.footer
   [span (:span footer-data)]
   [:ul.filters
    (for [filter-data (:filters footer-data)]
      [:li [anchor filter-data]])]
   (when (:showing? (:button footer-data))
     [button (:button footer-data)])
   footer-notice])

(defcomponent item [item-data]
  [:li {:class (:class item-data)}
   [:div.view
    [input (:input (:toggle item-data))]
    [label (:label item-data)]
    [button (:button item-data)]]
   (when (:showing? (:input item-data))
     [input (:input item-data)])])

;;;; Application Shell

(defcomponent shell [data]
  (let [list-data   (:list data)
        toggle-data (:toggle data)
        footer-data (:footer data)
        header-data (select-keys data [:header :input])]
    [:main.todoapp
     [header header-data]
     [:section.main
      (when (:showing? toggle-data)
        [:section.toggle
         [input (:input toggle-data)]
         [label (:label toggle-data)]])
      [:section.list
       [:ul.todo-list
        (for [item-data (:items list-data)]
          [item item-data])]]]
     (when (:showing? footer-data)
       [footer footer-data])]))