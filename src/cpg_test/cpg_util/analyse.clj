(ns cpg-test.cpg-util.analyse
    (:require [cpg-test.cpg-util.config :refer :all]
              [cpg-test.cpg-util.regex-creation :refer :all]
              [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.traversal :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)
             (de.fraunhofer.aisec.cpg.helpers SubgraphWalker)
             (java.util Set)))

(defn renameCallExpression "Renames a given CallExpression" [^CallExpression ex]
    (.setName ex (str (.getName ex) "+" (.hashCode ex))))

(defn analyse-args
    "
    Takes a sink and returns a tuple/list of (<Possible classloading Call arguments>) = (HashSet<String>, )
    "
    [^MultiValueEvaluator evaluator ^CallExpression sink ]
    (if (is-external-sink? sink)
        #{}    ;todo
        (as->
            ;returns single element list because only forName(x) and loadClass(x) have to be considered here
            (first (.getArguments sink)) n
            (.evaluate evaluator n)
            ;is always a Set<String>
            (if (instance? Set n) n #{n})))
    )

;(defn inter-proc-resolution)
;when defined, remember to remove old HashCode from CallExpression Name (which was added in the rename operation)
;when evaluating

(defn analyse-sink "Analyses a given Sink (CallExpression)" [^MultiValueEvaluator evaluator ^CallExpression sink]
    (do
        ;printing todo remove printing
        (prn "Name:" (.getFqn sink))
        (doseq [arg (.getArguments sink)]
            ;evaluated-arg can be a string or a hash-set
            (let [evaluated-arg (.evaluate evaluator arg)]
                (do
                    (prn evaluated-arg "---" (class evaluated-arg))
                    ;strings are the only valid objects because all class-loading sinks take one string as an argument
                    (doseq [traversed-arg (traverse-on-till arg [CallExpression] next-nodes-dfg 50)]
                        (prn evaluated-arg (.getName arg) "-" (.evaluate evaluator traversed-arg) (class traversed-arg))))))
        ;calculate
        (prn (analyse-args evaluator sink))
        ;todo call possible-loads-to-predicate with the result of analyse-args
        ;todo add method to generate additional information
        ))

(defn analyse
    "Entry Point to Analyse example file"
    [^String file]
    (let [
          config (get-config file)
          analyzer (get-analyzer config)
          ^TranslationResult result (get-translation-result analyzer)
          applicationNode (-> result
                              (.getAstChildren)
                              (.get 0))
          evaluator (MultiValueEvaluator.)
          nodes (SubgraphWalker/flattenAST applicationNode)
          call-expressions (filter #(instance? CallExpression %) nodes)
          sinks (filter is-sink? call-expressions)
          ]
        (do
            ;sinks is evaluated lazily, therefore call it before checking wi
            (doseq [sink sinks]
                (prn sink))
            ;rename CallExpressions
            (doseq [expr call-expressions]
                (renameCallExpression expr))
            ;(prn "Sinks:")
            (doseq [sink sinks]
                (analyse-sink evaluator sink)))))