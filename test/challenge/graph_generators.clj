(ns challenge.graph-generators
  (:require [clojure.test.check.generators :as gen]))

;; Helpers functions that generate a seq of edges on a graph in certain layout.

 
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


;; Generators

;; Generator for the number of vertices of a graph. 
;; Always greater than 1.
(def num-vertex (gen/such-that #(> % 1) gen/pos-int))

;; Generator for a complete graph.
(def complete-graph (gen/fmap complete-graph-fn num-vertex))

;; Generator for a line graph.
(def line-graph (gen/fmap line-graph-fn num-vertex))

;; Generator for a ring graph.
(def ring-graph (gen/fmap ring-graph-fn num-vertex))

;; Generator for a star graph.
(def star-graph (gen/fmap star-graph-fn num-vertex))

