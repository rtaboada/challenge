(ns challenge.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [challenge.core :refer :all]
            [challenge.graph :as graph]))


(deftest calculate-score-equals-initial-score-when-fraudulent-is-empty
  (testing "Small graph."
    (let [edges [[0 1] [0 2] [1 0] [1 3] [2 0] [3 1]]]
      (is (= (calculate-score edges [])
             (initial-score (graph/create-adjacency-matrix edges)))))))


(deftest factor-returns-correct-value
  (is (= (factor 1) 1/2))
  (is (= (factor 2) 3/4))
  (is (= (factor 3) 7/8))
  (is (= (factor 10) 1023/1024)))


(defn descending?
  [coll]
  (every? (fn [[a b]] (<= a b))
          (partition 2 1 coll)))

(defspec factor-scaling-is-descending
  (prop/for-all [v (gen/vector gen/pos-int)]
    (let [ks (sort v)]      
      (descending? (map factor ks)))))

