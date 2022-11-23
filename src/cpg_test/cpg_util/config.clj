(ns cpg-test.cpg-util.config
    (:require [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.sources :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager TranslationResult)
             (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)
             (de.fraunhofer.aisec.cpg.helpers SubgraphWalker)
             (de.fraunhofer.aisec.cpg.query QueryKt)
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
          call-expressions (filter #(instance? CallExpression %) nodes)
          sinks (filter is-sink? call-expressions)
          sources (filter is-source? call-expressions)
          ]
        (do
            (prn "Sinks:")
            (doseq [sink sinks]
                (prn "Name:" (.getFqn sink))
                (doseq [arg (.getArguments sink)]
                    (prn (.evaluate evaluator arg))))
            (prn "Sources:")
            (doseq [source sources]
                (prn "Name:" (.getFqn source)))
            (prn "Source-Sink-Paths")
            (doseq [source sources
                    sink sinks]
                (prn
                    "Source:" (.getFqn source)
                    "Sink:" (.getFqn sink)
                    ;checks if the source depends on any sink
                    (or (map #(.getValue (QueryKt/dataFlow source %)) (.getArguments sink))))))))