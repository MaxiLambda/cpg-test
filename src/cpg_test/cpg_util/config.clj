(ns cpg-test.cpg-util.config
    (:require [cpg-test.cpg-util.regex-creation :refer :all]
              [cpg-test.cpg-util.sinks :refer :all]
              [cpg-test.cpg-util.traversal :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager)
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




