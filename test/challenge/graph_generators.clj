(ns challenge.graph-generators)

 
(defn complete-graph
  "Return a seq with the edges of a complete graph with n vertices."
  [n]
  (for [x (range n) y (range n) :when (not= x y)]
    [x y]))


(defn- create-undirected-edge
  "Creates an undirected edge."
  [v1 v2]
  [[v1 v2] [v2 v1]])


(defn line-graph
  "Creates a line graph with n vertices."
  [n]
  (mapcat create-undirected-edge (range n) (range 1 n)))


(defn ring-graph
  "Creates a ring graph with n vertices."
  [n]
  (concat (line-graph n)
          (create-undirected-edge (dec n) 0)))


(defn star-graph
  "Creates a star graph with n vertices."
  [n]
  (mapcat create-undirected-edge (repeat 0) (range 1 n)))



