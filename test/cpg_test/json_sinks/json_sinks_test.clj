(ns cpg-test.json-sinks.json-sinks-test
  (:require [clojure.test :refer :all])
  (:require [cpg-test.json-sinks.json-sinks :refer [maps-to-map-by-name simple-map]]))

(deftest maps-to-map-by-name-test
  (is (= {"a" {:prefix 2 :suffix 3}} (maps-to-map-by-name [{"name" "a" "prefix" 2 "suffix" 3}] simple-map)))
  (is (= {"a" {:prefix 2 :suffix 3} "b" {:prefix 3 :suffix 4}} (maps-to-map-by-name [{"name" "a" "prefix" 2 "suffix" 3}{"name" "b" "prefix" 3 "suffix" 4}] simple-map))))