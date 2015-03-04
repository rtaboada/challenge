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
             (initial-score (graph/create-adjacency-matrix edges))))))
  (testing "File graph."
    (let [edges (graph/read-edge-file "edges")]
      (is (= (calculate-score edges [])
             (initial-score (graph/create-adjacency-matrix edges)))))))

(defspec calculate-score-equals-initial-score-if-fraudulent-is-empty
  50
  (prop/for-all 
   [edges (gen/fmap graph-test/complete-graph-edges
                    (graph-test/int-greater-1))]
   (= (calculate-score edges [])
      (initial-score (graph/create-adjacency-matrix edges)))))


(deftest factor-returns-correct-value
  (is (= (factor 1) 1/2))
  (is (= (factor 2) 3/4))
  (is (= (factor 3) 7/8))
  (is (= (factor 10) 1023/1024)))


(defn ascending?
  [coll]
  (every? (fn [[a b]] (<= a b))
          (partition 2 1 coll)))


(defspec factor-scaling-is-ascending
  (prop/for-all 
   [v (gen/vector gen/pos-int)]
   (let [ks (sort v)]
     (ascending? (map factor ks)))))

