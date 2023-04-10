(ns cpg-test.cli
    (:require [clojure.string :as str])
    (:import (java.io File)))

(defn is-path?
    "checks if a given string can represent a valid path" [^String path]
    (some? (try
                (.getCanonicalPath ^File (File. ^String path))
                (catch Exception _ nil))))
(def help-header "The following flags and default values are valid")
(defn exists? [^String path]
    (.exists (File. path)))
(def cli-options
    [
     ["-r" "--project-root ROOT" "Project root"
      :default "."
      :validate [exists? "Must be a an existing directory"]
      :id "project-root"]
     ["-j" "--jar-paths PATHS" "unmanaged .jar directories, separated by \";\""
      :default []
      :parse-fn #(str/split % #";")
      :validate [#(and (map exists? %)) "Must be a list of \";\" separated directories"]
      :id "jar-paths"]
     ["-s" "--external-sinks SINKS" "external sink definitions as .json files, separated by \";\""
      :default []
      :parse-fn #(str/split % #";")
      :validate [#(and (map exists? %)) "Must be a list of \";\" separated .json files"]
      :id "external-sinks"]
     ["-h" "--help"]])