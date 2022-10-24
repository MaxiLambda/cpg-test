(ns cpg-test.core
    (:import [de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager]
             [java.io File]
             [de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator]
             [de.fraunhofer.aisec.cpg.helpers SubgraphWalker]
             [de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression]))

(defn analyse
    "Entry Point to Analyse example file"
    []
    (let [
          config (-> (TranslationConfiguration/builder)
                     (.sourceLocations (java.util.List/of (File. "C:\\Users\\Maxi\\IdeaProjects\\analyse-project\\src\\root\\main")))
                     (.defaultPasses)
                     (.defaultLanguages)
                     (.build))
          analyzer (-> (TranslationManager/builder)
                       (.config config)
                       (.build))
          result (.get (.analyze analyzer))
          applicationNode (-> result
                              (.getAstChildren)
                              (.get 0))
          evaluator (MultiValueEvaluator.)
          nodes (SubgraphWalker/flattenAST applicationNode)
          ]
        (doseq [node nodes]
            (if (instance? CallExpression node)
                (do
                    (prn "Node: " node)
                    (prn "Name:" (.getFqn node))
                    (doseq [arg (.getArguments node)]
                        (prn arg)
                        (prn (.evaluate evaluator arg)))
                    )))
        ))

(defn -main [] (analyse))