(ns clj-holmes.logic.parser
  (:require [edamame.core :as edamame]
            [clj-holmes.logic.namespace :as logic.namespace]))

(defn ^:private alias-require? [require-declaration]
  (->> require-declaration
       second
       (= :as)))

(defn ^:private requires->auto-resolves-decl
  "Adapt requires from namespace declaration to a format used by edamame auto-resolve."
  [requires]
  (->> requires
       (filter alias-require?)
       (map #(remove keyword? %))
       (map reverse)
       (reduce concat)
       (apply hash-map)))

(defn ^:private auto-resolves
  "Parses the first form and if it is a namespace declaration returns a map containing all requires aliases."
  [code]
  (let [form (edamame/parse-string code {:all true})
        namespace (logic.namespace/name-from-ns-declaration form)
        ns-requires (logic.namespace/requires form)]
    (-> ns-requires
        requires->auto-resolves-decl
        (assoc :current namespace))))

(defn code->data
  "Receives a clojure file and returns all forms as data containing lines and rows metadata."
  [code]
  (let [auto-resolve (auto-resolves code)
        opts {:auto-resolve auto-resolve :all true :readers {'nu/time identity
                                                             'nu/date identity}}]
    (edamame/parse-string-all code opts)))

(comment
  (code->data "(ns banana) (+ 1 1)"))