(ns cpg-test.core
    (:import [de.fraunhofer.aisec.cpg TranslationConfiguration TranslationManager]
             [de.fraunhofer.aisec.cpg.graph GraphKt]
             (java.io File)
             (java.util List)))

(defn analyse
    "Entry Point to Analyse example file"
    []
    (let [
          config (-> (TranslationConfiguration/builder)
                     (.sourceLocations (List/of (File. "C:\\Users\\Maxi\\IdeaProjects\\cpg-maven-test\\src\\main\\java\\root\\Main.java")))
                     (.defaultPasses)
                     (.defaultLanguages)
                     (.build))
          analyzer (-> (TranslationManager/builder)
                       (.config config)
                       (.build))
          result (.get (.analyze analyzer))
          originCPG (GraphKt/getGraph result)
          nodes (.getNodes originCPG)
          ]
        (doseq [node nodes]
            (prn node))
        ))
