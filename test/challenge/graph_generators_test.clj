(ns challenge.graph-generators-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [challenge.graph :refer [vertices]]
            [challenge.graph-generators :refer :all]))


(deftest line-graph-test
  (testing "Line graph with 5 vertices"
    (let [graph (line-graph-fn 5)]
      (is (= graph
             [[0 1] [1 0] [1 2] [2 1] [2 3] [3 2] [3 4] [4 3]]))
      (is (= (count (vertices graph)) 5)))))


(deftest ring-graph-generation-test
  (testing "Ring graph with 9 vertices."
    (let [graph (ring-graph-fn 9)]
      (is (= graph
             [[0 1] [1 0] [1 2] [2 1] [2 3] [3 2] [3 4] [4 3] [4 5] [5 4] [5 6] [6 5] [6 7] [7 6] [7 8] [8 7] [8 0] [0 8]]))
      (is (= (count (vertices graph)) 9)))))


(deftest star-graph-generation-test
  (testing "Star graph with 8 vertices" 
    (let [graph (star-graph-fn 8)]
      (is (= graph
             [[0 1] [1 0] [0 2] [2 0] [0 3] [3 0] [0 4] [4 0] [0 5] [5 0] [0 6] [6 0] [0 7] [7 0]]))
      (is (= (count (vertices graph)) 8)))))


;; Check if the number of edges is the expected for a complete graph.
(defspec number-edges-complete-graph
  (prop/for-all
   [n num-vertex]
   (let [edges (complete-graph-fn n)]
     ;; Number of edges should be n*(n-1)/2 in a complete graph. 
     ;; But because of the way we represent the edges, they are counted twice.
     (= (* n (dec n)) (count edges)))))


;; Check if the number of vertices is the one expected for a complete graph.
(defspec number-vertices-in-complete-graph-is-n
  (prop/for-all 
   [n num-vertex]
   (= n (count (vertices (complete-graph-fn n))))))

