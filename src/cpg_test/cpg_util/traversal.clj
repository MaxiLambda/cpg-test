(ns cpg-test.cpg-util.traversal
    (:import (de.fraunhofer.aisec.cpg.graph Node)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)))

(defn traverse-on-till
    "
    Traverses the tree with a given generator function till a node of a given class is reached
    start - The Node on which the traversal shall start
    end-types - one of the possible Classes of the last Node to find
    next-node-function - a function which takes a node and returns a List of nodes (in Java Function<Node, Iterable<Node>>)
    "
    ([^Node start end-types next-nodes-function] (traverse-on-till start end-types next-nodes-function 20))
    ([^Node start end-types next-nodes-function max-depth]
     (loop [^Node to-do-nodes [start]
            result []
            depth 0
            visited #{}]
         (do
             (let [[current & remaining] to-do-nodes]
                 ;check if max-depth is reached or if there are no more nodes left to visit
                 (if (or (= depth max-depth) (empty? to-do-nodes))
                     result
                     ;check if the current node has already been visited
                     (if (contains? visited current)
                         (recur remaining result depth visited)
                         ;check if the current node is one of the final types
                         (if (some #(instance? % current) end-types)
                             (recur remaining (conj result current) (+ depth 1) (conj visited current))
                             (recur (concat remaining (next-nodes-function current)) result (+ depth 1) (conj visited current))))))))))

(defn next-nodes-dfg
    [^Node node]
    (.getPrevDFG node))

(defn traverse-on-dfg-till-call-expression ""
    [start max-depth]
    (traverse-on-till start [CallExpression] next-nodes-dfg max-depth))