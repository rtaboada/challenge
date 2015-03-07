(ns challenge.graph
  (:require [clojure.string :as string]))


;; Maximum distance possible in a graph.
(def max-dist Double/POSITIVE_INFINITY)


(defn- lines 
  "Split lines from the content."
  [content]
  (string/split content #"\n"))


(defn- read-file 
  "Auxiliary function for read-edge-file."
  [file-name]
  (map #(string/split %1 #" ")
       (lines (slurp file-name))))


(defn read-edge-file
  "Reads an edge file from disk. Expects the vertices id to be an Integer."
  [file-name]
  (map (fn [[v1 v2]]
         [(Integer/parseInt v1) (Integer/parseInt v2)]) 
       (read-file file-name)))


(defn vertices
  "Return the set of vertices defined by edges."
  [edges]
  (sort (set (apply concat edges))))


(defn adjacency-matrix
  "Return the adjacency matrix given the edges of the graph.
   Some assumptions:  

   1. the graph is undirected.
   2. the ids are compact, i.e without holes.
   3. first id is zero and last id is `(dec (count (vertices edges)))`"
  [edges]
  (let [neighbors (sort (group-by first edges))
        empty-row (vec (repeat (count (vertices edges))
                               max-dist))]
    (mapv #(apply assoc empty-row (mapcat vector
                                          (map second (second %1))
                                          (repeat 1)))
         neighbors)))

(def ^:private shortest-path
  (memoize
   (fn [g i j k]
     (if (= i j) ; same vertex, distance should be zero.
       0
       (if (neg? k)
         (get-in g [i j])
         (min (shortest-path g i j (dec k))
              (+ (shortest-path g i k (dec k))
                 (shortest-path g k j (dec k)))))))))


(defn floyd-warshall
  "All pairs shortest path distance graph algorithm,
   returns a distance matrix where the line and columns represent
   the same vertices as the adjacency matrix."
  [g]
  (let [v (count g)]
    (partition v v
               (for [x (range 0 v) y (range 0 v)]
                 (shortest-path g x y (dec v))))))


(defn closeness-centrality
  "Returns the closeness of every vertex in the graph, dist is a distance matrix.
   Return a seq with the closeness of every vertex in the graph."
  [dist]  
  (map #(/ 1. (apply + %1)) dist))

