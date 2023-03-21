(ns cpg-test.core
    (:require
        [cpg-test.cpg-util.analyse :refer :all]
        [cpg-test.maven-analysis.maven-dependency-analysis :refer [potentially-unused-dependencies]]
        [cpg-test.local-analysis.local-dependency-analysis :refer [local-dependencies]]))

(defn print-unused "prints unused dependencies" [unused]
    (do
        (prn "Unused Dependencies:")
        (prn unused)))

(defn find-unused "filters possibly unused dependencies, expects Map<String,List<String>>"
    [deps sink-accepts]
    (->> (filter #(->> %
                       (val)
                       (not-any? sink-accepts)) deps)
         (pmap key)))

(defn analyse-maven "Analyses the maven dependencies, returns a list of all unused ones"
    [^String project-root sink-accepts]
    (find-unused (potentially-unused-dependencies project-root) sink-accepts))

(defn analyse-local "Analyses all local dependencies, returns a list of all unused"
    [jar-paths sink-accepts]
    (find-unused (local-dependencies jar-paths) sink-accepts))

(defn start
    "actual -main method"
    [^String project-root jar-paths]
    (do
        (let [sink-accepts (analyse project-root)
              unused-deps (pvalues
                              (analyse-maven project-root sink-accepts)
                              (analyse-local jar-paths sink-accepts))]
            (prn "Maven")
            (print-unused (first unused-deps))
            (prn "Local")
            (print-unused (second unused-deps)))
        (shutdown-agents)))

(defn -main []
    (start
        "C:\\Users\\Maxi\\IdeaProjects\\MavenDepend"
        ["C:\\Users\\Maxi\\IdeaProjects\\MavenDepend\\out"]))