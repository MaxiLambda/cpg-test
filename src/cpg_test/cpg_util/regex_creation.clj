(ns cpg-test.cpg-util.regex-creation
    (:require [clojure.string :as str]
              [cpg-test.cpg-util.util :refer :all])
    (:import (java.util Set)
             (java.util.regex Pattern)))
(defn enclose "" [expr] (str "^" expr "$"))
(defn string-to-pattern
    "converts a string to the literal pattern"
    [s]
    (-> s Pattern/quote Pattern/compile))
(defn regex-partial-match
    "takes a regular expression (as an escaped string) describing a dependency and returns a regex match all sub-paths of this expression"
    [^String regex]
    (let [regex2 (str "&" regex "&")
          parts1 (str/split regex2 #"\\\.")
          joined1 (str/join "(\\.(" parts1)
          parts2 (str/split joined1 #"\.\*")
          joined2 (str/join "(.*(" parts2)
          closing (reduce str (take (- (* 2 (+ (count parts1) (count parts2))) 3) (repeat ")?")))]
        (-> (str "(" joined2 closing)
            (str/replace #"&" "")
            (str/replace #"\(\)\?" ""))))
(defn to-regex
    "
    Takes a String of a possible value for class loading, and how to resolve some CallExpression resolutions.
    The resolutions can be a string or a Set<String>
    The String should only contain [a-Z0-9_${}.] and each { should be paired with } and never nested
    resolution Map<{call},value>
    "

    [possibility resolution ]
    (loop [remainder possibility]
        (if (str/includes? remainder "{")
            (let [opening-index (str/index-of remainder "{")
                  closing-index (str/index-of remainder "}")
                  call (subs remainder opening-index (+ closing-index 1))
                  call-pattern (string-to-pattern call)]
                (if (contains? resolution call)
                    (let [resolved-res (get resolution call)
                          ready-res (if (instance? Set resolved-res)
                                        ;if the resolution value is a set, turn the set in its own regular expression
                                        ;str/re-quote-replacement is necessary to prevent unexpected behaviour from $
                                        (as-> (map str/re-quote-replacement resolved-res) n
                                              (str/join "|" n)
                                              (to-regex n resolution)
                                              ;replace '.' with ',' so it is not handled in the regex-to-partial-match call
                                              (str/replace n #"\." ","))
                                        (str/re-quote-replacement resolved-res))]
                        (recur (str/replace-first remainder call-pattern ready-res)))
                    (recur (str/replace-first remainder call-pattern "!"))))
            (-> remainder
                (str/replace #"[$.]" "\\\\$0")
                (str/replace #"!" ".*")
                (regex-partial-match)
                (str/replace #"," "\\\\.")
                ;this function is used for debugging only
                ;((fn [s] (do
                ;             (prn s)
                ;             s)))
                )))
    )
(defn finish-regex "" [possibility resolution ]
    (-> (to-regex possibility resolution)
        (enclose)
        (Pattern/compile)))
(defn possible-loads-to-predicate
    "
    Takes a Set<String> of possible values for class loading, and how to resolve some CallExpression resolutions.
    A String from the set should only contain [a-Z0-9_${}.] and each { should be paired with } and never nested.
    Returns a list of Predicates
    "
    [possibilities resolution]
    (reduce-preds-with-or (map #(.asMatchPredicate (finish-regex %1 resolution)) possibilities)))