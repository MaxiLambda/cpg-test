(ns cpg-test.cpg-util.util
    (:import (java.util.function Predicate)))

(defn false-predicate
    "Returns a Predicate which is always false"
    []
    ;reify creates a java object implementing a given Interface
    (reify Predicate
        (test [_ _]
            false))
    )

(defn reduce-preds-with-or
    "
    reduces a list of Predicates with or.
    If the list is empty, it returns a predicate with always returns false.
    "
    [preds]
    (if (= 0 (count preds))
        (false-predicate)
        (reduce #(.or %1 %2) preds))
    )

