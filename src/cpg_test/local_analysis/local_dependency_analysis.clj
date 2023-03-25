(ns cpg-test.local-analysis.local-dependency-analysis
    (:require [clojure.java.io :as io]
              [clojure.java.shell :as sh]
              [clojure.string :as str])
    (:import (java.io File)))

(def class-pattern ".class")
(def jar-pattern #".\.[jw]ar")
(defn local-dependencies-find
    "Returns all .jar/ .war files in the directories specified in paths as File"
    [paths]
    (->> paths
         (map io/file)
         (filter #(.isDirectory %))
         (map file-seq)
         (flatten)
         (filter #(not (nil? (re-find jar-pattern (.getName ^File %)))))))

(defn filter-jar-output
    "filters the output of a 'jar tf ...' command for class files"
    [^String jar-output]
    (->> (str/split-lines jar-output)
         (filter #(str/ends-with? % class-pattern))
         (map #(str/replace % #"[/\\]" "."))
         (map #(str/replace % #"\.class$" ""))))
(defn classes-of-jar
    "returns a list of the classnames of all classes from a jar"
    [^File jar-file]
    (filter-jar-output (:out (sh/sh "resources/scripts/local-jar-list-classes.bat" (.getPath ^File jar-file)))))

(defn local-dependencies
    "
    Returns a map of <jar-name> <List<included classnames>>
    "
    [paths]
    (let [jars (local-dependencies-find paths)
          classes (map #(classes-of-jar %) jars)]
        (zipmap (map #(.getName ^File %) jars) classes)))