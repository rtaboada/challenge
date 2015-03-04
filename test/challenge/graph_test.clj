(ns challenge.graph-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]            
            [challenge.graph :refer :all]))

;; Some utilities

(defn int-greater-1 
  "Returns a generator of positive integers greater than 1."
  []
  (gen/such-that #(> % 1) gen/pos-int))


(defn complete-graph-edges 
  "Return a seq with the edges of a complete graph with n vertices."
  [n]
  (for [x (range n) y (range n) :when (not= x y)]
    [x y]))


(defn complete-graph 
  "Returns the adjacency matrix of a complete graph with n vertices."
  [n]
  (create-adjacency-matrix (complete-graph-edges n)))


;; Check if the number of vertices is the one expected for a complete graph.
(defspec number-vertices-in-complete-graph-is-n
  (prop/for-all 
   [n (int-greater-1)]
   (= n (count (vertices (complete-graph-edges n))))))


;; Check if the number of edges is the expected for a complete graph.
(defspec number-edges-complete-graph
  (prop/for-all
   [n (int-greater-1)]
   (let [edges (complete-graph-edges n)]
     ;; Number of edges should be n*(n-1)/2 in a complete graph. 
     ;; But because of the way we represent the edges, they are counted twice.
     (= (* n (dec n)) (count edges)))))


;; Test if the size of the adjacency matrix is correct, based on number of vertices.
(defspec size-adjacency-matrix
  (prop/for-all
   [n (int-greater-1)]
   (let [graph (complete-graph n)]
     ;; Size of adjacency matrix should be equal to n.
     (and (= n (count graph))
          (every? #(= n (count %1))
                  graph)))))


;; Test if the shortest path algorithm performs correctly in a complete graph.
(defspec max-distance-complete-graph
  50
  (prop/for-all 
   [graph (gen/fmap complete-graph (int-greater-1))]
   (let [dist (floyd-warshall graph)]      
     ;; Max distance should be equal to 1 in a complete graph.            
     (= 1 (apply max (map #(apply max %1) dist))))))


;; Test if closeness is calculated correctly in a complete graph.
(defspec closeness-complete-graph
  50
  (prop/for-all 
   [n (int-greater-1)]
   (let [graph (complete-graph n)
         closeness (closeness-centrality (floyd-warshall graph))]
     ;; Every vertex should have the same closeness in a complete graph.
     (every? #(= (first closeness) %1) closeness))))


;; Test if the value calculated for closeness is correct for a complete graph.
(defspec closeness-value-complete-graph
  50
  (prop/for-all
   [n (int-greater-1)]
   (let [graph (complete-graph n)
         closeness (closeness-centrality (floyd-warshall graph))]
     ;; The value of closeness should be 1/(n-1) in a complete graph.
     (= (first closeness) (/ 1 (dec n))))))
