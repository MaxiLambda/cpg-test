(ns cpg-test.core
    (:require [clojure.string :as str])
    (:import (de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph Node)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)
             (de.fraunhofer.aisec.cpg.helpers SubgraphWalker)
             (java.io File)
             (java.util List)
             (lincks.maximilian.clojure.kotlin MainKt)
             ))

(def class-for-name "java.lang.Class.forName")

(def load-class-function-name ".loadClass")
;(defn test []
;    (Maxi/test))

(defn test2 []
    (MainKt/main)
    ;(.sayHi (Human .))
    )

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

(defn analyse
    "Entry Point to Analyse example file"
    [^String file]
    (let [
          config (-> (TranslationConfiguration/builder)
                     (.sourceLocations (List/of (File. file)))
                     (.addIncludesToGraph true)
                     (.defaultPasses)
                     (.defaultLanguages)
                     (.build))
          analyzer (-> (TranslationManager/builder)
                       (.config config)
                       (.build))
          ^TranslationResult result (.get (.analyze analyzer))
          applicationNode (-> result
                              (.getAstChildren)
                              (.get 0))
          evaluator (MultiValueEvaluator.)
          nodes (SubgraphWalker/flattenAST applicationNode)
          ]
        (doseq [node nodes]
            (if (and
                    (instance? CallExpression node)
                    (or
                        (is-for-name-call? node)
                        (is-load-class-call? node)))
                (do
                    (prn "Name:" (.getFqn node))
                    (doseq [arg (.getArguments node)]
                        (prn (.evaluate evaluator arg)))
                    )))
        ))

(defn -main []
    ;(analyse "C:\\Users\\Maxi\\IdeaProjects\\analyse-project\\src\\root\\main")
    (test2)
    )

