(ns cpg-test.cpg-util.sinks
    (:require [clojure.string :as str])
    (:import (de.fraunhofer.aisec.cpg.graph Node)))

(def class-for-name "java.lang.Class.forName")

(def load-class-function-name ".loadClass")

(defn is-for-name-call?
    "Checks if the Node is a call to \"java.lang.Class.forName\""
    [^Node node]
    (= class-for-name (.getFqn node)))

(defn is-load-class-call?
    "Checks if the Node is a function named loadClass (TODO: write check if method is called by ClassLoader or Subclasses)"
    [^Node node]
    (let [fqn (.getFqn node)]
        (and
            (not (nil? fqn))
            (str/ends-with? fqn load-class-function-name))))

(defn is-sink?
    (some-fn is-for-name-call? is-load-class-call?))