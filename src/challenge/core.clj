(ns challenge.core
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [challenge.graph :as graph]))


(defn initial-score 
  "Returns a seq with the initial score of all vertices in the graph."
  [graph]
  (-> graph
      graph/floyd-warshall
      graph/closeness-centrality))


(defn factor 
  "Scaling factor applied to a vertex score,
   k is the distance between the fraudulent vertex and the other vertex."
  [k]
  (- 1 (Math/pow 1/2 k)))


(defn fraudulent 
  "Marks a vertex v as fraudulent, v score will be zero and every 
   other vertex's score will be changed by a factor dependent on 
   their distance to the fraudulent vertex."
  [score dist v]
  (->> v
       (nth dist)
       (map factor)
       (map * score)))


(defn calculate-score
  "Returns a seq with the current score of all vertices in the graph."
  [edges fraudulents]
  (let [graph (graph/create-adjacency-matrix edges)
        dist (graph/floyd-warshall graph)
        score (initial-score graph)]
    (loop [score score fs fraudulents]
      (if-not (seq fs)
        score
        (recur (fraudulent score dist (first fs)) (rest fs))))))


(defn score
  "Returns a seq with a tuple of the form [vertex-id score] sorted by score."
  [edges fraudulents]
  (->> (calculate-score edges fraudulents)
       (map vector (range))
       (sort-by second)
       (reverse)))

