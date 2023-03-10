(ns cpg-test.cpg-util.sinks
    (:require [clojure.string :as str])
    (:import (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression MemberCallExpression StaticCallExpression)))


(def class-for-name "java.lang.Class.forName")

(defn is-for-name-call?
    "Checks if the CallExpression is a call to \"java.lang.Class.forName\""
    [^CallExpression call-expression]
    (and
        (= class-for-name (.getFqn call-expression))
        (instance? StaticCallExpression call-expression)))

(def load-class-function-name ".loadClass")
(defn is-load-class-call?
    "Checks if the Node is a function named loadClass"
    [^CallExpression call-expression]
    (let [fqn (.getFqn call-expression)]
        (and
            (not (nil? fqn))
            (str/ends-with? fqn load-class-function-name)
            (instance? MemberCallExpression call-expression)
            (= 1 (.size (.getSignature call-expression))))))

(defn is-sink? [^CallExpression call-expression]
    ((some-fn is-for-name-call? is-load-class-call?) call-expression))