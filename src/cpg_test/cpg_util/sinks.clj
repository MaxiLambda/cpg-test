(ns cpg-test.cpg-util.sinks
    (:require [clojure.string :as str])
    (:import (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)))

(def class-for-name "java.lang.Class.forName")

(def load-class-function-name ".loadClass")

(defn is-for-name-call?
    "Checks if the CallExpression is a call to \"java.lang.Class.forName\""
    [^CallExpression call-expression]
    (= class-for-name (.getFqn call-expression)))

(defn is-load-class-call?
    "Checks if the Node is a function named loadClass (TODO: write check if method is called by ClassLoader or Subclasses)"
    [^CallExpression call-expression]
    (let [fqn (.getFqn call-expression)]
        (and
            (not (nil? fqn))
            (str/ends-with? fqn load-class-function-name))))

(defn is-sink? [^CallExpression call-expression]
    ((some-fn is-for-name-call? is-load-class-call?) call-expression))