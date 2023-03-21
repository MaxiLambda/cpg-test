(ns cpg-test.core
    (:require
        [cpg-test.cpg-util.analyse :refer :all]
        [cpg-test.maven-analysis.maven-dependency-analysis :refer :all]))

(defn print-unused "prints unused dependencies" [unused]
    (do
        (prn "Unused Dependencies:")
        (prn unused)))
(defn start
    "actual -main method"
    [^String file jar-paths]
    (do
        (->> (potentially-unused-dependencies file)
             (filter #(->> %
                           (val)
                           (not-any? (analyse file))))
             (map key)
             (print-unused))
        (shutdown-agents)))

(defn -main []
    (start "C:\\Users\\Maxi\\IdeaProjects\\MavenDepend" []))