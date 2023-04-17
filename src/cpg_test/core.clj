(ns cpg-test.core
    (:gen-class)
    (:require
        [clojure.tools.cli :as prs]
        [cpg-test.cpg-util.analyse :refer :all]
        [cpg-test.cli :refer [cli-options help-header]]
        [cpg-test.local-analysis.local-dependency-analysis :refer [local-dependencies]]
        [cpg-test.maven-analysis.maven-dependency-analysis :refer [potentially-unused-dependencies]]))

(defn print-unused "prints unused dependencies" [unused]
    (do
        (println "Unused Dependencies:")
        (println unused)))

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
    [^String project-root jar-paths external-sink-definitions]
    (do
        (let [sink-accepts (analyse project-root external-sink-definitions)
              unused-deps (pvalues
                              (analyse-maven project-root sink-accepts)
                              (analyse-local jar-paths sink-accepts))]
            (println "Maven")
            (print-unused (first unused-deps))
            (println "Local")
            (print-unused (second unused-deps)))
        (shutdown-agents)))

;example configuration
;-r C:\Users\Maxi\IdeaProjects\MavenDepend -j C:\Users\Maxi\IdeaProjects\MavenDepend\out -s C:\Users\Maxi\IdeaProjects\MavenDepend\sinks.json
(defn -main [& args]
    (let [{:keys [options summary errors]} (prs/parse-opts args cli-options)
          {:strs [project-root jar-paths external-sinks]} options
          {help :help} options]
        (if help
            (do
                (println help-header)
                (println summary))
            (if errors
                (println errors)
                (do
                    (println "Starting")
                    (println "Project-root:" project-root)
                    (println "External-Jars:" jar-paths)
                    (println "Sink definitions:" external-sinks)
                    (start project-root jar-paths external-sinks))))))