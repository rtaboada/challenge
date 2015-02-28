(ns challenge.core
  (:require [clojure.string :as string]
            [clojure.set :as set]))


(defn read-file [file-name]
  (map #(string/split %1 #" ")
       (string/split (slurp file-name) #"\n")))

(def g-test (mapv (fn [xs] 
                    (mapv #(if (zero? %1) 
                             1e10M 
                             1) 
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
         (get-in g [(dec j) (dec i)])
         (min (shortest-path g i j (dec k))
              (+ (shortest-path g i k (dec k))
                 (shortest-path g k j (dec k)))))))))

(defn floyd-warshall
  "All pairs shortest path distance graph algorithm"
  [g]
  (let [v (count g)]
    (partition v v
               (for [x (range 1 (inc v)) y (range 1 (inc v)) ]
                 (shortest-path g x y v)))))

(defn closeness-centrality
  "Returns the closeness of every vertex in the graph"
  [dist]  
  (map #(/ 1 (apply + %1)) dist))


(defn initial-score [g]
  (-> g
      floyd-warshall
      closeness-centrality))


(defn factor 
  "Coefficient that varies"
  [k]
  (rationalize (- 1 (Math/pow 1/2 k))))


(defn fraudulent 
  "Marks a vertex v as fraudulent, the vertex score will be zero and every other vertex's score will be changed by a factor dependent on their distance to the fraudulent vertex."
  [score dist v]
  (map * 
       score 
       (map factor (nth dist v))))


