(ns cpg-test.cpg-util.traversal
    (:import (de.fraunhofer.aisec.cpg.graph Node)))

(defn traverse-on-till
    "
    Traverses the tree with a given generator function till a node of a given class is reached
    start - The Node on which the traversal shall start
    end-types - one of the possible Classes of the last Node to find
    next-node-function - a function which takes a node and returns a List of nodes (in Java Function<Node, Iterable<Node>>)
    "
    [^Node start end-types next-nodes-function]
    (loop [^Node to-do-nodes [start]
           result []
           depth 0]
        (do
            ;(prn "To-Do:" to-do-nodes)
            ;(prn "Result:" result)

            (let [[current & remaining] to-do-nodes]
                (if (or (= depth 20) (empty? to-do-nodes))
                    result
                    (if (some #(instance? % current) end-types)
                        (recur remaining (conj result current) (+ depth 1))
                        (recur (concat remaining (next-nodes-function current)) result (+ depth 1))))))))

(defn next-nodes-dfg
    [^Node node]
    (.getPrevDFG node))