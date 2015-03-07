(ns challenge.graph-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [challenge.graph-generators :as gen-graph]
            [challenge.graph :refer :all]))

;; ## Utilities ##

(defn equal-items? 
  "Checks if every item in the collection is equal."
  [coll]
  (every? #(= (first coll) %1) coll))


(defn ordered-coll 
  "Returns true if the `cmp-fn` returns true for every comparison.
  The comparison function is called for every pair in a sliding window of size 2.
  For example with `(ordered-coll [1 3 5 7] <)` the `cmp-fn` would be called with the
  following arguments: `[1 3] [3 5] [5 7]`.

  Use this function to verify if a collection is:  

  - Strictly increasing (passing < as `cmp-fn`)
  - Strictly decreasing (with >)
  - Non-increasing (with <=)
  - Non-decreasing (with >=)"
  [cmp-fn coll]
  (every? (fn [[a b]] (cmp-fn a b))
          (partition 2 1 coll)))

;; ## Tests ##

;; Test if the size of the adjacency matrix is correct, based on number of vertices.
(defspec size-adjacency-matrix
  (prop/for-all
   [edges gen-graph/graph]
   (let [graph (adjacency-matrix edges)
         n (count (vertices edges))]
     ;; Size of adjacency matrix should be equal to n.
     (and (= n (count graph))
          (every? #(= n (count %1))
                  graph)))))


;; Test if the _shortest path algorithm_ performs correctly in a complete graph.
(defspec max-distance-complete-graph
  50
  (prop/for-all 
   [edges gen-graph/complete-graph]
   (let [dist (floyd-warshall (adjacency-matrix edges))]      
     ;; Max distance should be equal to 1 in a complete graph.            
     (= 1 (apply max 
                 (map #(apply max %1) dist))))))


;; Test if **closeness** is calculated correctly in a complete graph.
(defspec closeness-complete-graph
  50
  (prop/for-all 
   [edges gen-graph/complete-graph]
   (let [graph (adjacency-matrix edges)
         closeness (closeness-centrality (floyd-warshall graph))]
     ;; Every vertex should have the same **closeness** in a complete graph.
     (equal-items? closeness))))


;; Test if the value calculated for **closeness** is correct for a complete graph.
(defspec closeness-value-complete-graph
  50
  (prop/for-all
   [edges gen-graph/complete-graph]
   (let [n (count (vertices edges))
         graph (adjacency-matrix edges)
         closeness (closeness-centrality (floyd-warshall graph))]
     ;; The value of closeness should be 1/(n-1) in a complete graph.
     (= (first closeness) (/ 1. (dec n))))))


;; **Closeness** should be equal for all vertices in a ring graph.
(defspec closeness-ring-graph
  50
  (prop/for-all
   [edges gen-graph/ring-graph]
   (let [graph (adjacency-matrix edges)
         closeness (closeness-centrality (floyd-warshall graph))]
     (equal-items? closeness))))


(defn closeness-value-ring-graph
  "Closed formula that calculates the **closeness** value in a ring graph of `n` vertices.
   The key insight is that every vertex in the ring graph as a distance vector of the form:
   `[0 1..n/2 n/2..1]` if `n` is odd and `[0 1..(n/2 - 1) n/2 (n/2 - 1)..1]` if `n` is even.
   The sum of a sequence `1..n` is equal to `n * (n+1) / 2`, but we have two sequences 
   and so the formula simplifies to: `n * (n+1)`"
  [n]
  (let [half-n (Math/floor (/ n 2))
        farness (if (even? n)
                  (+ half-n ; The lonely `n/2` in the even distance vector
                     (* (dec half-n) half-n)) ; 2 times the sequence 1..(n-1)
                  (* half-n (inc half-n)))] ; 2 times the sequence 1..n
    (/ farness)))


;; Checks if the **closeness** calculated on a ring graph is the same 
;; as the **closeness** calculated by the closed formula.
(defspec closeness-ring-graph-value-is-correct
  50
  (prop/for-all
   [edges gen-graph/ring-graph]
   (let [graph (adjacency-matrix edges)]
     (= (closeness-value-ring-graph (count (vertices edges)))
        (first (closeness-centrality (floyd-warshall graph)))))))


;; Checks that every outer vertex has the same **closeness** and 
;; that the center vertex has the highest **closeness** when `n > 2`, 
;; and the same **closeness** when the star graph degenerates to 
;; a line graph with two vertices.
(defspec closeness-star-graph-properties
  50
  (prop/for-all
   [edges gen-graph/star-graph]
   (let [closeness (-> edges
                       adjacency-matrix
                       floyd-warshall
                       closeness-centrality)]
     (and (equal-items? (rest closeness))
          (if (> (count closeness) 2) 
            (> (first closeness) (second closeness)) ; star graph with 3 or more vertices.
            (= (first closeness) (second closeness))))))) ; line graph with 2 vertices.


;; Checks that the center vertex has the highest 
;; **closeness** in a line graph.
(defspec closeness-line-graph-properties
  50
  (prop/for-all
   [edges gen-graph/line-graph]
   (let [closeness (-> edges
                       adjacency-matrix
                       floyd-warshall
                       closeness-centrality)]
     (= (apply max closeness)
        (nth closeness (/ (count closeness) 2))))))


;; Checks that in the outer vertices of the graph the distance vector is 
;; strictly crescent or decrescent and that they the same when ordered. 
(defspec distance-vector-line-graph-properties
  50
  (prop/for-all
   [edges gen-graph/line-graph]
   (let [distance (-> edges
                      adjacency-matrix
                      floyd-warshall)
         first-dist-vector (first distance)
         last-dist-vector (last distance)]
     (and (ordered-coll < first-dist-vector)
          (ordered-coll > last-dist-vector)
          (= (sort first-dist-vector)
             (sort last-dist-vector))))))
