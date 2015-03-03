(ns challenge.graph-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]            
            [challenge.graph :refer :all]))


(defn complete-graph [n]
  (create-adjacency-matrix
   (for [x (range n) y (range n) :when (not= x y)]
     [x y])))


(defspec max-dist-complete-graph-is-1
  (prop/for-all [graph (gen/fmap complete-graph 
                                 (gen/such-that #(> %1 1) gen/pos-int))]
    (let [dist (floyd-warshall graph)]      
      (= 1
         (apply max (map #(apply max %1) dist))))))

