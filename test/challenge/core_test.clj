(ns challenge.core-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [challenge.core :refer :all]
            [challenge.graph :as graph]
            [challenge.graph-generators :as graph-gen]
            [challenge.graph-test :as graph-test]))


;; ## `initial-score` and `calculate-score` tests

(deftest calculate-score-equals-initial-score-when-fraudulent-is-empty
  (testing "Small graph."
    (let [edges [[0 1] [0 2] [1 0] [1 3] [2 0] [3 1]]]
      (is (= (calculate-score edges [])
             (initial-score (graph/adjacency-matrix edges))))))
  (testing "File graph."
    (let [edges (graph/read-edge-file "edges")]
      (is (= (calculate-score edges [])
             (initial-score (graph/adjacency-matrix edges)))))))


;; Checks if `initial-score` is equals to the score calculated by 
;; `calculate-score`, when no vertex is flagged as fraudulent.
(defspec calculate-score-equals-initial-score-if-fraudulent-is-empty
  50
  (prop/for-all 
   [edges graph-gen/graph]
   (= (calculate-score edges [])
      (initial-score (graph/adjacency-matrix edges)))))


;; Checks if `initial-score` is different of the score returned by
;; `calculate-score`, when some vertex is flagged as _fraudulent_.
(defspec calculate-score-not=-initial-score-when-fraudulent-not-empty
  50
  (prop/for-all
   [edges-fs graph-gen/graph-and-fraudulents]
   (let [[edges fraudulents] edges-fs]
     (not= (calculate-score edges fraudulents)
           (initial-score (graph/adjacency-matrix edges))))))

;; Checks if the `calculate-score` function returns the expected
;; score when the center vertex is flagged as _fraudulent_ in a _star_ graph.
(defspec star-graph-with-fraudulent-center
  50
  (prop/for-all
   [edges graph-gen/star-graph]
   (let [old-score (calculate-score edges [])
         new-score (calculate-score edges [0])]
     (and (= 0.0 (nth new-score 0)) ; score of center vertex is zero.
          (every? identity (map #(= %1 (* 2 %2)) ; others vertex score is halved.
                                 (rest old-score)
                                 (rest new-score)))))))


;; Verify that every vertex flagged as _fraudulent_ as a zero score.
(defspec every-fraudulent-vertex-score-is-zero
  50
  (prop/for-all
   [es-fs graph-gen/graph-and-fraudulents]
   (let [[edges fraudulents] es-fs
         score (calculate-score edges fraudulents)]
     (every? zero?
             (map #(nth score %1) fraudulents)))))

;; Verify that score is non-increasing when fraudulents vertices is included.
(defspec score-cant-increase-with-fraudulents-vertices
  50
  (prop/for-all
   [es-fs graph-gen/graph-and-fraudulents]
   (let [[edges fraudulents] es-fs
         initial-score (calculate-score edges [])
         current-score (calculate-score edges fraudulents)]
     (every? identity (map >= initial-score current-score)))))


;; ## `factor` tests

;; Simple test that checks some values return by `challenge.core/factor`.
(deftest factor-returns-correct-value
  (is (= (factor 1) 0.5) "Factor of 1 (immediate neighbors) should be 1/2")
  (is (= (factor 2) 0.75))
  (is (= (factor 3) 0.875))
  (is (= (factor 10) 0.9990234375)))


;; Checks if `factor` keeps the invariant of always returning 
;; a bigger scaling factor when the distance `k` increases.
(defspec factor-scaling-is-ascending
  (prop/for-all 
   [v (gen/vector gen/pos-int)]
   (let [ks (sort v)]
     (graph-test/ordered-coll <= (map factor ks)))))

