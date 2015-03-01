(ns challenge.graph
  (:require [clojure.string :as string]))


(defn- lines [content]
  (string/split content #"\n"))


(defn- read-file [file-name]
  (map #(string/split %1 #" ")
       (lines (slurp file-name))))


(defn- parse-edge [[v1 v2]]
  [(Integer/parseInt v1) (Integer/parseInt v2)])


(defn read-edge-file [file-name]
  (map parse-edge (read-file file-name)))

(defn create-adjacency-matrix [edges]
  (let [vertices (set (apply concat edges))
        neighbors (group-by first edges)
        empty-row (vec (repeat (count vertices) 1e10M))]
    (mapv #(apply assoc empty-row (mapcat vector 
                                          (map second (second %1))
                                          (repeat 1)))
         neighbors)))

(def ^:private shortest-path
  (memoize
   (fn [g i j k]
      (if (= i j)
        0
        (if (zero? k)
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
  (map #(/ 1 (apply + %1)) dist))
