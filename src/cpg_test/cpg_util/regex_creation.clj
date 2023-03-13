(ns cpg-test.cpg-util.regex-creation
    (:require [clojure.string :as str]
              [cpg-test.cpg-util.util :refer :all])
    (:import (java.util.regex Pattern)))

(defn string-to-pattern
    "converts a string to the literal pattern"
    [s]
    (-> s Pattern/quote Pattern/compile))

(defn to-regex
    "
    Takes a String of a possible value for class loading, and how to resolve some CallExpression resolutions.
    The String should only contain [a-Z0-9_${}.] and each { should be paired with } and never nested
    resolution Map<{call},value>
    "
    [possibility resolution ]
    (loop [remainder possibility]
        (if (str/includes? remainder "{")
            (let [opening-index (str/index-of remainder "{")
                  closing-index (str/index-of remainder "}")
                  ;
                  call (subs remainder opening-index (+ closing-index 1))
                  call-pattern (string-to-pattern call)
                  ]
                (if (contains? resolution call)
                    (recur (str/replace-first remainder call-pattern (str/re-quote-replacement (get resolution call))))
                    (recur (str/replace-first remainder call-pattern "?"))))
            (-> remainder
                (str/replace #"[$.]" "\\\\$0")
                (str/replace #"\?" ".*")
                Pattern/compile
                ;this function is used for debugging only
                ((fn [s] (do
                             (prn s)
                             s))))))
    )

(defn possible-loads-to-predicate
    "
    Takes a Set<String> of possible values for class loading, and how to resolve some CallExpression resolutions.
    A String from the set should only contain [a-Z0-9_${}.] and each { should be paired with } and never nested.
    Returns a list of Predicates
    "
    [possibilities resolution]
    (reduce-preds-with-or (map #(.asMatchPredicate (to-regex %1 resolution)) possibilities)))