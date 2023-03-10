(ns cpg-test.cpg-util.config
    (:require [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.traversal :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager TranslationResult)
             (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression Literal)
             (de.fraunhofer.aisec.cpg.helpers SubgraphWalker)
             (java.io File)
             (java.util List)))

(defn get-config [^String file]
    (-> (TranslationConfiguration/builder)
        (.sourceLocations (List/of (File. file)))
        (.addIncludesToGraph true)
        (.defaultPasses)
        (.defaultLanguages)
        (.build)))

(defn get-analyzer [^TranslationConfiguration config]
    (-> (TranslationManager/builder)
        (.config config)
        (.build)))

(defn get-translation-result [^TranslationManager analyzer]
    (.get (.analyze analyzer)))

(defn renameCallExpression "Renames a given CallExpression" [^CallExpression ex]
    (.setName ex (str (.getName ex) "+" (.hashCode ex))))

(defmulti print-handler type)
(defmethod print-handler Literal [node]
    (str "+" (.getValue node) "+"))
(defmethod print-handler CallExpression [node]
    (str "+" (.getName node) "+"))

; some CallExpressions have invokes Edges
; (CallExpression)-invokes-> (FunctionDeclaration) <- DFG- (ReturnStatement) <- Return_Value- (<Expression to Evaluate>)
; <Expression to evaluate> is the expression which can be tried to evaluate
(defn expr-invokes "Returns the FunctionDeclaration invoked by the CallExpression" [^CallExpression expr]
    (.getInvokes expr))
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
            ;rename CallExpressions
            (doseq [expr call-expressions]
                (renameCallExpression expr))
            (prn "Sinks:")
            (doseq [sink sinks]
                (prn "Name:" (.getFqn sink))
                (doseq [arg (.getArguments sink)]
                    ;evaluated-arg can be a string or a hash-set
                    (let [evaluated-arg (.evaluate evaluator arg)]
                        (do
                            (prn evaluated-arg "---" (class evaluated-arg))
                            ;strings are the only valid objects because all class-loading sinks take one string as an argument
                            (doseq [traversed-arg (traverse-on-till arg [Literal CallExpression] next-nodes-dfg 50)]
                                (prn evaluated-arg (print-handler traversed-arg) (.evaluate evaluator traversed-arg) (class traversed-arg))))))))))

