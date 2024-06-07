# todomvc-dumdom
Todo MVC implemented in ClojureScript using Christian Johansen's [dumdom](https://github.com/cjohansen/dumdom).

Feature complete _(as far as TodoMVCs go)_. Includes support for:
*  Adding, editing, searching and filtering todos
*  Serializing and restoring from browser local state
*  TodoMVC app CSS styles (https://github.com/tastejs/todomvc-app-css)

To get familiar with dumdom, I recommend the [official README](https://github.com/cjohansen/dumdom) and the creator's [talk on data-driven UIs](https://2023.javazone.no/program/85f23370-440f-42b5-bf50-4cb811fef44d). The coding styles discussed therein guided the design of this MVC. All mutability and state change in the app is contained in `main.cljs`, and that in these 5 lines:

```clojure
(dumdom/set-event-handler!
 (fn [event actions]
   (reset! store (model/handle-action action event @store))))

(add-watch store ::re-render (fn [_ _ _ _] (render!)))

(add-watch store ::save-state (fn [_ _ _ _] (save-state! storage-key)))
```
