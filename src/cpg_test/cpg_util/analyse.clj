(ns cpg-test.cpg-util.analyse
    (:require [cpg-test.cpg-util.config :refer :all]
              [cpg-test.cpg-util.regex-creation :refer :all]
              [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.traversal :refer :all]
              [cpg-test.cpg-util.util :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph.statements ReturnStatement)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions BinaryOperator CallExpression Expression)
             (de.fraunhofer.aisec.cpg.helpers SubgraphWalker)
             (java.util Set)))

(defn renameCallExpression "Renames a given CallExpression" [^CallExpression ex]
    (.setName ex (str (.getName ex) "-" (.hashCode ex))))

;TODO add Operators maybe
(defmulti resolve-node (fn [_ node] (class node)))

;"Tries to resolve the return statement of a CallExpression"
;the following evaluates to "stuff2" instead of <wildcard> or a hashset
;public static String other(String s){
;   if(s.length() > 3){
;       return "stuff";
;   }
;   return "stuff2";
;}
(defmethod resolve-node CallExpression
    [^MultiValueEvaluator evaluator ^CallExpression expression]
    ;(CallExpression)-invokes-> (FunctionDeclaration) <- DFG- (ReturnStatement) <- RETURN_VALUE - Expression
    (->> expression
         (.getInvokes)
         (first)
         (.getPrevDFG)
         (first)
         (.getReturnValue)
         (.evaluate evaluator))
    )
ReturnStatement

(defn dependent-calls
    "Tries to find all CallExpressions that a List of Nodes may rely on.
    Returns a list of those expressions"
    [args]
    (reduce concat (map #(traverse-on-dfg-till-call-expression %1 20) args)))
(defn inter-proc-resolution
    "
    tries to resolve arguments inter-procedural
    "
    [^MultiValueEvaluator evaluator ^CallExpression sink]
    (let [args (.getArguments sink)
          dependent (dependent-calls args)
          relevant (filter #(instance? CallExpression %1) (distinct (concat dependent args)))
          nodes (map #(str "{" (.getName %1) "}") relevant)
          values (map #(resolve-node evaluator %1) relevant)]
        ;without the vec call the sequence is lazy which leads to a runtime error
        (into {} (filter #(not (nil? (second %1))) (zipmap nodes values)))))

(defn analyse-args
    "
    Takes a sink and returns a tuple/list of (<Possible classloading Call arguments>) = (HashSet<String>, )
    Returns a set of strings, which are the possible classloading arguments
    "
    [^MultiValueEvaluator evaluator ^CallExpression sink]
    (if (is-external-sink? sink)
        #{}    ;todo
        (as->
            ;returns single element list because only forName(x) and loadClass(x) have to be considered here
            (first (.getArguments sink)) n
            (.evaluate evaluator n)
            ;is always a Set<String>
            (if (instance? Set n) n #{n})
            (do
                (prn n)
                n)))
    )


;when defined, remember to remove old HashCode from CallExpression Name (which was added in the rename operation)
;when evaluating
(defn analyse-sink
    "
    Analyses a given Sink (CallExpression).
    Returns a List<Predicate>.
    "
    [^MultiValueEvaluator evaluator ^CallExpression sink]
    (do
        (prn "Name:" (.getFqn sink))
        ;calculate
        (possible-loads-to-predicate
            (analyse-args evaluator sink)
            (inter-proc-resolution evaluator sink))))

(defn get-unused-dependencies
    "returns a List<String> containing all unused dependencies of the analysed project"
    []
    ;todo write function that fetches all unused dependencies
    ;   maybe pass them as program argument
    '(
        "org.apache.commons:commons-lang"
        "org.apache.commons:commons-text"
        "org.apache.commons:commons-collections"
        "root.my.stuff.something.Abc$ClassName"
        "root.my.stuff2.something.Abc$ClassName"
         "com.root.Maxi"
         "de.root.Maxi"
         "fr.root.Maxi"
         )
    )

(defn analyse
    "
    Entry Point to Analyse example file
    Returns a List<String> of all dependencies which do match any possible sink call"
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
            ;sinks is evaluated lazily, therefore call it before checking renaming
            ;otherwise the sink-checks with fqn do not work
            (prn "Count sinks:" (count sinks))
            ;rename CallExpressions
            (doseq [expr call-expressions]
                (renameCallExpression expr))
            (let [matches-sink-preds (map #(analyse-sink evaluator %) sinks)
                  matches-any-sink? (reduce-preds-with-or matches-sink-preds)]
                (prn "Unused deps:"
                     (filter #(not (.test matches-any-sink? %1)) (get-unused-dependencies)))))))