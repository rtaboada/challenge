(ns challenge.graph-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [challenge.graph-generators :refer :all]
            [challenge.graph :refer :all]))


(defn int-greater-1 
  "Returns a generator of positive integers greater than 1."
  []
  (gen/such-that #(> % 1) gen/pos-int))


(deftest line-graph-test
  (testing "Line graph with 5 vertices"
    (let [graph (line-graph 5)]
      (is (= graph
             [[0 1] [1 0] [1 2] [2 1] [2 3] [3 2] [3 4] [4 3]]))
      (is (= (count (vertices graph)) 5)))))


(deftest ring-graph-generation-test
  (testing "Ring graph with 9 vertices."
    (let [graph (ring-graph 9)]
      (is (= graph
             [[0 1] [1 0] [1 2] [2 1] [2 3] [3 2] [3 4] [4 3] [4 5] [5 4] [5 6] [6 5] [6 7] [7 6] [7 8] [8 7] [8 0] [0 8]]))
      (is (= (count (vertices graph)) 9)))))


(deftest star-graph-generation-test
  (testing "Star graph with 8 vertices" 
    (let [graph (star-graph 8)]
      (is (= graph
             [[0 1] [1 0] [0 2] [2 0] [0 3] [3 0] [0 4] [4 0] [0 5] [5 0] [0 6] [6 0] [0 7] [7 0]]))
      (is (= (count (vertices graph)) 8)))))


;; Check if the number of vertices is the one expected for a complete graph.
(defspec number-vertices-in-complete-graph-is-n
  (prop/for-all 
   [n (int-greater-1)]
   (= n (count (vertices (complete-graph n))))))


;; Check if the number of edges is the expected for a complete graph.
(defspec number-edges-complete-graph
  (prop/for-all
   [n (int-greater-1)]
   (let [edges (complete-graph n)]
     ;; Number of edges should be n*(n-1)/2 in a complete graph. 
     ;; But because of the way we represent the edges, they are counted twice.
     (= (* n (dec n)) (count edges)))))


;; Test if the size of the adjacency matrix is correct, based on number of vertices.
(defspec size-adjacency-matrix
  (prop/for-all
   [n (int-greater-1)]
   (let [graph (adjacency-matrix (complete-graph n))]
     ;; Size of adjacency matrix should be equal to n.
     (and (= n (count graph))
          (every? #(= n (count %1))
                  graph)))))


;; Test if the shortest path algorithm performs correctly in a complete graph.
(defspec max-distance-complete-graph
  50
  (prop/for-all 
   [graph (gen/fmap (comp adjacency-matrix complete-graph) (int-greater-1))]
   (let [dist (floyd-warshall graph)]      
     ;; Max distance should be equal to 1 in a complete graph.            
     (= 1 (apply max (map #(apply max %1) dist))))))


(defn equal-items? 
  "Checks if every item in the collection is equal."
  [coll]
  (every? #(= (first coll) %1) coll))

;; Test if closeness is calculated correctly in a complete graph.
(defspec closeness-complete-graph
  50
  (prop/for-all 
   [n (int-greater-1)]
   (let [graph (adjacency-matrix (complete-graph n))
         closeness (closeness-centrality (floyd-warshall graph))]
     ;; Every vertex should have the same closeness in a complete graph.
     (equal-items? closeness))))


;; Test if the value calculated for closeness is correct for a complete graph.
(defspec closeness-value-complete-graph
  50
  (prop/for-all
   [n (int-greater-1)]
   (let [graph (adjacency-matrix (complete-graph n))
         closeness (closeness-centrality (floyd-warshall graph))]
     ;; The value of closeness should be 1/(n-1) in a complete graph.
     (= (first closeness) (/ 1. (dec n))))))


;; Closeness should be equal in a ring graph.
(defspec closeness-ring-graph
  50
  (prop/for-all
   [n (int-greater-1)]
   (let [graph (adjacency-matrix (ring-graph n))
         closeness (closeness-centrality (floyd-warshall graph))]
     (equal-items? closeness))))


(defn closeness-value-ring-graph
  "Formula that calculates the closeness value in a ring graph of n vertices.
   The key insight is that every vertex in the ring graph as a distance vector of the form:
   [0 1..n n..1] if n is odd and [0 1..(n-1) n (n-1)..1] if n is even."
  [n]
  (let [half-n (Math/floor (/ n 2))
        factor (if (even? n) half-n 0)]
    (/ (- (* half-n (inc half-n)) factor))))

(defspec closeness-ring-graph-value-is-correct
  50
  (prop/for-all
   [n (int-greater-1)]
   (let [graph (adjacency-matrix (ring-graph n))]
     (= (closeness-value-ring-graph n)
        (first (closeness-centrality (floyd-warshall graph)))))))
