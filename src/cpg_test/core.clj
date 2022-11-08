(ns cpg-test.core
    (:require [cpg-test.cpg-util.config :refer :all])
    (:import (de.fraunhofer.aisec.cpg TranslationResult)
             (de.fraunhofer.aisec.cpg.graph Node)
             (de.fraunhofer.aisec.cpg.query QueryKt)))

(defn -main []
    ;(analyse "C:\\Users\\Maxi\\IdeaProjects\\analyse-project\\src\\root\\main")
    (let [
          config (get-config "C:\\Users\\Maxi\\IdeaProjects\\analyse-project\\src\\root\\main")
          analyzer (get-analyzer config)
          ^TranslationResult result (.get (.analyze analyzer))
          applicationNode (-> result
                              (.getAstChildren)
                              (.get 0))]
        (do
            (prn
                (instance? Node result)
                (instance? Node applicationNode)
                (instance? Node (-> result (.getAstChildren) (.get 0))))
            (prn (QueryKt/dataFlow result applicationNode))
            )))

