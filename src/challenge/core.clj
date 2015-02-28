(ns challenge.core
  (:require [clojure.string :as string]
            [clojure.set :as set]))


(defn read-file [file-name]
  (map #(string/split %1 #" ")
       (string/split (slurp file-name) #"\n")))


(def g-test (mapv (fn [xs] 
                    (mapv #(if (zero? %1) 
                             1e10M 
                             %1) 
                          xs)) 
                  [[0  8  15  9   0  0  0   0]
                   [8  0  13  0   0  0  0   0]
                   [15 13 0   0   6  0  5   0]
                   [9  0  0   0  13  0  0   0]
                   [0  0  6  13   0  7  0   0]
                   [0  0  0   0   7  0  9   4]
                   [0  0  5   0   0  9  0   18]
                   [0  0  0   0   0  4  18  0]]))

(def shortest-path
  (memoize 
   (fn [g i j k]
     (if (= i j)
       0
       (if (zero? k)
         (get-in g [(dec i) (dec j)])
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
               (for [x (range 1 (inc v)) y (range 1 (inc v)) ]
                 (shortest-path g x y v)))))

(defn closeness-centrality
  "Returns the closeness of every vertex in the graph"
  [dist]  
  (map #(/ 1 (apply + %1)) dist))


(defn initial-score 
  "Returns a seq with the initial score of all vertices in the graph."
  [g]
  (-> g
      floyd-warshall
      closeness-centrality))


(defn factor 
  "Scaling factor applied to a vertex score,
   k is the distance between the fraudulent vertex and the other vertex."
  [k]
  (rationalize (- 1 (Math/pow 1/2 k))))


(defn fraudulent 
  "Marks a vertex v as fraudulent, v score will be zero and every 
   other vertex's score will be changed by a factor dependent on 
   their distance to the fraudulent vertex."
  [score dist v]
  (->> v
       (nth dist)
       (map factor)
       (map * score)))

