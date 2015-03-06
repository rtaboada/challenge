(ns challenge.graph
  (:require [clojure.string :as string]))


(def max-dist 1e10M)

(defn- lines [content]
  (string/split content #"\n"))


(defn- read-file [file-name]
  (map #(string/split %1 #" ")
       (lines (slurp file-name))))


(defn- parse-edge [[v1 v2]]
  [(Integer/parseInt v1) (Integer/parseInt v2)])


(defn read-edge-file
  "Reads an edge file from disk. "
  [file-name]
  (map parse-edge (read-file file-name)))


(defn vertices
  "Return the set of vertices defined by edges."
  [edges]
  (sort (set (apply concat edges))))


(defn create-adjacency-matrix
  "Creates an adjacency matrix given the edges of the graph.
   Some assumptions: - the graph is undirected.
                     - the ids are compact, i.e without holes.
                     - first id is zero and last id is (dec (count (vertices)))"
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
     (if (= i j)
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
  (map #(/ (apply + %1)) dist))

