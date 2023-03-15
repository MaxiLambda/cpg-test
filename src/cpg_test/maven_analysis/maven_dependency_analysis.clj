(ns cpg-test.maven-analysis.maven-dependency-analysis
    (:require [clojure.java.shell :as sh]
              [clojure.string :as str]))

; $1 - groupId
; $2 - artifactId
; $3 - version
(def dependency-pattern #"\[WARNING]\s*([^:]*):([^:]*):[^:]*:([^:]*)")
(defn package-from-output
    "
    Strips the added output from the package: \"[WARNING]    commons-collections:commons-collections:jar:3.2.2:compile\"
    "
    [^String package-line]
    (re-find dependency-pattern package-line))
(def start-undeclared "[WARNING] Unused declared dependencies found")
(def end-section "[INFO] ---")
(defn strip-mvn-dependency-analyze
    "
    extracts the unused dependencies from the maven dependency:analyze call
    returns a list of lists, each inner list contains the groupId, artifactId and version of an unused dependency
    "
    [^String mvn-output]
    (->> (str/split-lines mvn-output)
         (drop-while #(not (str/includes? % start-undeclared)))
         (drop 1)
         (take-while #(not (str/includes? % end-section)))
         (map package-from-output)
         (map rest)
         (map #(str/join ":" %)))
    )
(defn mvn-dependency-analyze
    "
    Takes the path to a maven project with the org.apache.maven.plugins maven-dependency-plugin added.
    Executes the dependency:analyze goal. The result is returned as a string.
    "
    [^String path]
    (strip-mvn-dependency-analyze (:out (sh/sh "scripts/maven-dependency-analyze.bat" path))))

(def class-pattern #"\[INFO]\s*([a-zA-Z\d._$]*)")
(def start-classes "[INFO] --- maven-dependency-plugin")
(defn class-from-output
    "
    Strips added output from the class: \"[INFO] org.slf4j.spi.MDCAdapter\".
    Returns the fully qualified class-name
    "
    [^String class-line]
    (re-find class-pattern class-line)
    )
(defn strip-mvn-dependency-list
    "
    extracts all classes from the mvn dependency:list-classes call
    returns a list of classnames as Strings
    "
    [^String mvn-output]
    (->> (str/split-lines mvn-output)
         (drop-while #(not (str/includes? % start-classes)))
         (drop 1)
         (take-while #(not (str/includes? % end-section)))
         (map class-from-output)
         (map second)
         )
    )
(defn mvn-dependency-list-classes
    "
    Takes the path to a maven project with the org.apache.maven.plugins maven-dependency-plugin added.
    Second argument is the dependency in the form groupId:artifactId:version of the dependency to execute the command with.
    Executes the dependency:list-classes -Dartifact=<groupId:artifactId:version> goal.
    "
    [^String path dependency]
    (strip-mvn-dependency-list (:out (sh/sh "scripts/maven-dependency-list-classes.bat" path dependency)))
    )

(defn potentially-unused-dependencies
    "
    Returns a map of <dependency-name> <List<included classnames>>
    "
    [^String path]
    (let [potentially-unused (mvn-dependency-analyze path)
          classes (map #(mvn-dependency-list-classes path %) potentially-unused)]
        (zipmap potentially-unused classes)))