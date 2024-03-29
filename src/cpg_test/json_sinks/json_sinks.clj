(ns cpg-test.json-sinks.json-sinks
    (:require
        [clojure.data.json :as json]
        [clojure.math.combinatorics :as combo])
    (:import (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)
             (java.util Set)))

(defn simple-map
    "a mapper function for maps-to-map-by-name"
    [map]
    (let [{name "name"
           pre  "prefix"
           suf  "suffix"} map]
        {name {:prefix pre :suffix suf}}))
(defn pattern-map
    "a mapper function for maps-to-map-by-name"
    [map]
    {(get map "name") (get map "pattern")})
(defn maps-to-map-by-name "" [maps mapper]
    (apply merge (map mapper maps)))
(defn get-json-sink-config
    "Expects a list of paths where json - configuration files can be found, providing a name twice is undefined behaviour"
    [paths]
    (let [sinks (->> (map slurp paths)
                     (map json/read-str))
          simple (filter some? (flatten (map #(get % "simple") sinks)))
          pattern (filter some? (flatten (map #(get % "pattern") sinks)))
          simple-res (maps-to-map-by-name simple simple-map)
          pattern-res (maps-to-map-by-name pattern pattern-map)]
        {:simple            simple-res
         :pattern           pattern-res
         :simple-contains?  #(contains? simple-res (.toString (.getName %)))
         :pattern-contains? #(contains? pattern-res (.toString (.getName %)))}))


(defn analyse-external-sink
    "
    json-sink-config contains all information about external sinks.
    Any CallExpression which is passed to this function should satisfy
    simple-contains? or pattern-contains? from get-json-sink-config
    "
    [json-sink-config ^MultiValueEvaluator evaluator ^CallExpression sink]
    (let [{simple           :simple
           pattern          :pattern
           simple-contains? :simple-contains?
           } json-sink-config]
        (if (simple-contains? sink)
            (let [{prefix :prefix
                   suffix :suffix} (get simple (.toString (.getName sink)))]
                (as->
                    ;returns single element list
                    ;because only the first argument of a simple-sink is relevant
                    (first (.getArguments sink)) n
                    (.evaluate evaluator n)
                    ;turns n into Set<String>
                    (if (instance? Set n) n #{n})
                    (set (map #(str prefix % suffix) n))))
            ;sink is expected to pass pattern-contains?
            (let [load-pattern (get pattern (.toString (.getName sink)))]
                (as-> (.getArguments sink) n
                      (map #(.evaluate evaluator %) n)
                      ;turns n into Collection<Set<String>>
                      (map #(if (instance? Set %) % #{%}) n)
                      ;gets all possible combinations of equated parameters
                      (apply combo/cartesian-product n)
                      (set (map #(apply (partial format load-pattern) %) n)))))))
