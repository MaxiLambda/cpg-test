(ns cpg-test.cpg-util.sources
    (:import (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)))

(def files-lines-name "java.nio.file.Files.lines")

(defn is-files-lines-call?
    [^CallExpression call-expression]
    (= files-lines-name (.getFqn call-expression)))

(defn is-source? [^CallExpression call-expression]
    ((some-fn is-files-lines-call?) call-expression))