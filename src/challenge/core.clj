(ns challenge.core
  "Problem specific functions are in this namespace."
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [challenge.graph :as graph]))


(defn factor 
  "Scaling factor applied to a vertex score,
  `k` is the distance between the fraudulent vertex and the 
  vertex whose score is being updated."
  [k]
  (- 1 (Math/pow 1/2 k)))


(defn fraudulent 
  "Marks a vertex `v` as fraudulent, `v` score will be zero and every 
  other vertex's score will be changed by a factor dependent on 
  their distance to the fraudulent vertex.  
  `score` is a `seq` with current score and `dist` is the distance matrix.
  Returns the new score after applying `factor` to every vertex."
  [score dist v]
  (->> v
       (nth dist)
       (map factor)
       (map * score)))

 
(defn initial-score 
  "Returns a seq with the initial score of all vertices in the graph.
  The initial score is simply the **closeness centrality** of the vertex."
  [graph]
  (-> graph
      graph/floyd-warshall
      graph/closeness-centrality))


(defn calculate-score
  "Returns a `seq` with the current score of all vertices in the graph.
  `edges` are a `set` with all edges in the graph and `fraudulents` is a 
  `set` with all the vertices flagged as _fraudulent_"
  [edges fraudulents]
  (let [graph (graph/adjacency-matrix edges)
        dist (graph/floyd-warshall graph)
        score (initial-score graph)]
    (loop [score score fs fraudulents]
      (if-not (seq fs)
        score
        (recur (fraudulent score dist (first fs)) (rest fs))))))


(defn score
  "Returns a `seq` with a tuple of the form `[vertex-id score]` sorted by score."
  [edges fraudulents]
  (->> (calculate-score edges fraudulents)
       (map vector (range))
       (sort-by second)
       (reverse)))

