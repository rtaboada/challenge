(ns challenge.graph-generators
  (:require [clojure.test.check.generators :as gen]
            [challenge.graph :as graph]))

;; ## Helpers
;; Functions that generate a seq of edges on a graph with certain layout.

 
(defn complete-graph-fn
  "Return a seq with the edges of a complete graph with n vertices."
  [n]
  (for [x (range n) y (range n) :when (not= x y)]
    [x y]))


(defn- create-undirected-edge
  "Creates an undirected edge."
  [v1 v2]
  [[v1 v2] [v2 v1]])


(defn line-graph-fn
  "Creates a line graph with n vertices."
  [n]
  (mapcat create-undirected-edge (range n) (range 1 n)))


(defn ring-graph-fn
  "Creates a ring graph with n vertices."
  [n]
  (concat (line-graph-fn n)
          (create-undirected-edge (dec n) 0)))


(defn star-graph-fn
  "Creates a star graph with n vertices."
  [n]
  (mapcat create-undirected-edge (repeat 0) (range 1 n)))


;; ## Generators

;; Generator for the number of vertices of a graph. 
;; Always greater than 1.
(def num-vertex (gen/such-that #(> % 1) gen/pos-int))

;; Generator for a set of vertices in the graph.
(defn choose-vertices
  "Generates a set of vertices with ids in `(range 0 n)` "
  [n]
  (gen/fmap set
            (gen/not-empty (gen/vector (gen/choose 0 (dec n))))))

;; Generator for a complete graph.
(def complete-graph (gen/fmap complete-graph-fn num-vertex))

;; Generator for a line graph.
(def line-graph (gen/fmap line-graph-fn num-vertex))

;; Generator for a ring graph.
(def ring-graph (gen/fmap ring-graph-fn num-vertex))

;; Generator for a star graph.
(def star-graph (gen/fmap star-graph-fn num-vertex))

;; Generator that chooses one of the graph layouts:
;;
;; - Complete
;; - Line
;; - Ring
;; - Star
(def graph (gen/one-of [complete-graph line-graph ring-graph star-graph]))

;; Generator of a graph and fraudulents vertices.
;; Generates a tuple with the list of edges and a set of fraudulents vertices.
(def graph-and-fraudulents
  (gen/bind graph
            (fn [edges]
              (gen/tuple (gen/return edges)
                         (choose-vertices (count (graph/vertices edges)))))))

