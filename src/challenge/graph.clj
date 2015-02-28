(ns challenge.graph)

(def ^:private shortest-path
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
  "Returns the closeness of every vertex in the graph, dist is a distance matrix.
   Return a seq with the closeness of every vertex in the graph."
  [dist]  
  (map #(/ 1 (apply + %1)) dist))

