(ns cpg-test.cpg-util.sinks
    (:require [clojure.string :as str])
    (:import (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression MemberCallExpression)))

;the fraunhofer cpg tool resolves the name of Class.forName calls as "Class.forName"
;this is, in my opinion, incorrect and should be "java.lang.Class.forName"
(def class-for-name "java.lang.Class.forName")
(def short-class-for-name "Class.forName")
(defn is-for-name-call?
    "Checks if the CallExpression is a call to \"java.lang.Class.forName\""
    [^CallExpression call-expression]
    (and
        (or ;necessary because of weird name resolution of the fraunhofer cpg tool
            (= short-class-for-name (.toString (.getName call-expression)))
            (= class-for-name (.toString (.getName call-expression))))
        (instance? MemberCallExpression call-expression)
        (.isStatic ^MemberCallExpression call-expression)))

(def load-class-function-name ".loadClass")
(defn is-load-class-call?
    "Checks if the Node is a function named loadClass"
    [^CallExpression call-expression]
    (let [fqn (.toString (.getName call-expression))]
        (and
            (some? fqn)
            (str/ends-with? fqn load-class-function-name)
            (instance? MemberCallExpression call-expression)
            (= 1 (.size (.getSignature call-expression))))))
(defn is-internal-sink? [^CallExpression call-expression]
    ((some-fn is-for-name-call? is-load-class-call?) call-expression))

(defn is-sink? "external patterns are the patterns which describe non default sinks"
    [is-external-sink? ^CallExpression call-expression]
    ((some-fn is-internal-sink? is-external-sink?) call-expression))