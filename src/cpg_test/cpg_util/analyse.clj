(ns cpg-test.cpg-util.analyse
    (:require [cpg-test.cpg-util.config :refer :all]
              [cpg-test.cpg-util.regex-creation :refer :all]
              [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.traversal :refer :all]
              [cpg-test.cpg-util.util :refer :all]
              [cpg-test.json-sinks.json-sinks :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.analysis MultiValueEvaluator)
             (de.fraunhofer.aisec.cpg.graph Name)
             (de.fraunhofer.aisec.cpg.graph Node)
             (de.fraunhofer.aisec.cpg.graph Name)
             (de.fraunhofer.aisec.cpg.graph.statements.expressions CallExpression)
             (de.fraunhofer.aisec.cpg.helpers SubgraphWalker)
             (java.util Set)))

(defn renameCallExpression "Renames a given CallExpression" [^CallExpression ex]
    (let [name (.getName ex)
          localName (.getLocalName name)
          parent (.getParent name)
          delimiter (.getDelimiter name)
          newName (str localName "-" (.hashCode ex))]
        (.setName ex (Name. newName parent delimiter))))

(defn resolve-node
    "Tries to resolve the return statement of a CallExpression"
    ;the following evaluates to "stuff2" instead of <wildcard> or a hashset
    ;public static String other(String s){
    ;   if(s.length() > 3){
    ;       return "stuff";
    ;   }
    ;   return "stuff2";
    ;}
    [^MultiValueEvaluator evaluator ^CallExpression expression]
    (let [invokes (.getInvokes expression)]
        (if (empty? invokes)
            ;the Node can't be resolved due to the definition not being part of the analyzed code
            ;Nodes with external FunctionDeclarations have no invokes Edges
            ;return a string as if the Evaluator could not gather any information
            (str "{" (.getName ^Node expression) "}")
            ;(CallExpression)-invokes-> (FunctionDeclaration) <- DFG- (ReturnStatement) <- RETURN_VALUE - Expression
            (->> invokes
                 (first)
                 (.getPrevDFG)
                 (first)
                 (.getReturnValue)
                 (.evaluate evaluator)))))

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
          nodes (map #(str "{" (.toString (.getName %1)) "}") relevant)
          values (map #(resolve-node evaluator %1) relevant)]
        ;without the vec call the sequence is lazy which leads to a runtime error
        (into {} (filter #(some? (second %1)) (zipmap nodes values)))))

(defn analyse-internal-sink "" [^MultiValueEvaluator evaluator ^CallExpression sink]
    (as->
        ;returns a list of arguments, only ClassLoader.loadClass and Class.forName have to be considered
        ;the relevant argument of each call is denoted by x, irrelevant ones by _
        ;when the function takes 1 or 3 arguments, the first one is relevant
        ;when the function takes 2 arguments, the second one is relevant
        ;  ClassLoader.loadClass(x)
        ;  Class.forName(x)
        ;  Class.forName(_,x)
        ;  Class.forName(x,_,_)
        (.getArguments sink) n
        (if (not= (count n) 2)
            (first n)
            (second n))
        (.evaluate evaluator n)
        ;is always a Set<String>
        (if (instance? Set n) n #{n})))
(defn analyse-args
    "
    Takes a sink and returns a tuple/list of (<Possible classloading Call arguments>) = (HashSet<String>, )
    Returns a set of strings, which are the possible classloading arguments.
    Only defined for sinks which satisfy is-internal-sink? or is-external-sink?
    "
    [json-sink-config ^MultiValueEvaluator evaluator ^CallExpression sink]
    (if (is-internal-sink? sink)
        (analyse-internal-sink evaluator sink)
        (analyse-external-sink json-sink-config evaluator sink)))

;when defined, remember to remove old HashCode from CallExpression Name (which was added in the rename operation)
;when evaluating
(defn analyse-sink
    "
    Analyses a given Sink (CallExpression).
    Returns a List<Predicate>.
    "
    [json-sink-config ^MultiValueEvaluator evaluator ^CallExpression sink]
    (do
        (println "Sink-Name:" (.toString (.getName sink)))
        ;calculate
        (possible-loads-to-predicate
            (analyse-args json-sink-config evaluator sink)
            (inter-proc-resolution evaluator sink))))

(defn analyse
    "
    Entry Point to analyse a project, paths contains the paths to json-sink configurations
    Returns a IFn String -> Bool which evaluates to true if the given string may be dynamically loaded
    by the project under file"
    [^String file paths]
    (let [
          config (get-config file)
          analyzer (get-analyzer config)
          ^TranslationResult result (get-translation-result analyzer)
          applicationNode (-> result
                              (.getAstChildren)
                              (.get 0))
          evaluator (MultiValueEvaluator.)
          nodes (.flattenAST (SubgraphWalker/INSTANCE) applicationNode)
          call-expressions (filter #(instance? CallExpression %) nodes)
          sink-config (get-json-sink-config paths)
          is-external-sink? (some-fn (:simple-contains? sink-config) (:pattern-contains? sink-config))
          sinks (filter (partial is-sink? is-external-sink?) call-expressions)
          is-possibly-called (reduce-preds-with-or (map (fn [sink] (analyse-sink sink-config evaluator sink)) sinks))]
        (do
            ;sinks is evaluated lazily, therefore call it before checking renaming
            ;otherwise the sink-checks with fqn do not work
            (println "Count sinks:" (count sinks))
            ;rename CallExpressions
            (doseq [expr call-expressions] (renameCallExpression expr))
            (fn [class] (.test is-possibly-called class)))))