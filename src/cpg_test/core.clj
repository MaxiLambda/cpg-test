(ns cpg-test.core
    (:require
        [cpg-test.cpg-util.analyse :refer :all]
        [cpg-test.maven-analysis.maven-dependency-analysis :refer :all]))

(defn print-unused "prints unused dependencies" [unused]
    (do
        (prn "Unused Dependencies:")
        (newline)
        (prn unused)))
(defn -main []
    (do
        (let [file "C:\\Users\\Maxi\\IdeaProjects\\MavenDepend"
              may-be-loaded? (analyse file)]
            (->> (potentially-unused-dependencies file)
                 (filter #(->> %
                              (val)
                              (not-any? may-be-loaded?)))
                 (map key)
                 (prn)))
        (shutdown-agents))
)
