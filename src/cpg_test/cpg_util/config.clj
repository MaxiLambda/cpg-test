(ns cpg-test.cpg-util.config
    (:require [cpg-test.cpg-util.regex-creation :refer :all]
              [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.traversal :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression Literal)
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

(defmulti print-handler (fn [node arg] (type node)))
(defmethod print-handler Literal [node arg]
    (str "+" (.getName arg) "+"))
(defmethod print-handler CallExpression [node arg]
    (str "+" (.getName node) "+"))

; some CallExpressions have invokes Edges
; (CallExpression)-invokes-> (FunctionDeclaration) <- DFG- (ReturnStatement) <- Return_Value- (<Expression to Evaluate>)
; <Expression to evaluate> is the expression which can be tried to evaluate
(defn expr-invokes "Returns the FunctionDeclaration invoked by the CallExpression" [^CallExpression expr]
    (.getInvokes expr))



