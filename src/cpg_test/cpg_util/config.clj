(ns cpg-test.cpg-util.config
    (:require [cpg-test.cpg-util.sinks :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager TranslationResult)
             (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)
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
          sinks (filter (every-pred #(instance? CallExpression %) (is-sink?)) nodes)
          ]
        (doseq [sink sinks]
            (prn "Name:" (.getFqn sink))
            (doseq [arg (.getArguments sink)]
                (prn (.evaluate evaluator arg))))))