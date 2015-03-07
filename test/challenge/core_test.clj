(ns challenge.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [challenge.core :refer :all]
            [challenge.graph :as graph]
            [challenge.graph-test :as graph-test]))


(deftest calculate-score-equals-initial-score-when-fraudulent-is-empty
  (testing "Small graph."
    (let [edges [[0 1] [0 2] [1 0] [1 3] [2 0] [3 1]]]
      (is (= (calculate-score edges [])
             (initial-score (graph/adjacency-matrix edges))))))
  (testing "File graph."
    (let [edges (graph/read-edge-file "edges")]
      (is (= (calculate-score edges [])
             (initial-score (graph/adjacency-matrix edges)))))))


;; Generator for the number of vertices of a graph.
(def num-vertex (graph-test/int-greater-1))


;; Checks if initial-score is equals to the score calculated by 
;; calculate-score, when no vertex is flagged as fraudulent.
(defspec calculate-score-equals-initial-score-if-fraudulent-is-empty
  50
  (prop/for-all 
   [edges (gen/fmap graph-test/complete-graph-edges num-vertex)]
   (= (calculate-score edges [])
      (initial-score (graph/adjacency-matrix edges)))))


;; Checks if initial-score is different of the score returned by
;; calculate-score, when some vertex is flagged as fraudulent.
(defspec calculate-score-should-change-initial-score-when-fraudulent-is-not-empty
  50
  (prop/for-all
   [fvertex-and-edges (gen/bind num-vertex 
                                 (fn [n] 
                                   (gen/tuple (gen/choose 0 (dec n)) 
                                              (gen/return (graph-test/complete-graph-edges n)))))]
   (let [[flagged-vertex edges] fvertex-and-edges]
     (not= (calculate-score edges [flagged-vertex])
           (initial-score (graph/adjacency-matrix edges))))))


;; Simple test that checks some values return by challenge.core/factor.
(deftest factor-returns-correct-value
  (is (= (factor 1) 0.5) "Factor of 1 (immediate neighbors) should be 1/2")
  (is (= (factor 2) 0.75))
  (is (= (factor 3) 0.875))
  (is (= (factor 10) 0.9990234375)))


;; Returns true if coll is in non-descending order.
(def ascending? (partial graph-test/ordered-coll <=))


;; Checks if factor keeps the invariant of always returning 
;; a bigger scaling factor when the distance k increases.
(defspec factor-scaling-is-ascending
  (prop/for-all 
   [v (gen/vector gen/pos-int)]
   (let [ks (sort v)]
     (ascending? (map factor ks)))))

