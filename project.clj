(defproject cpg-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [de.fraunhofer.aisec/cpg-core "5.1.0"]
                 [de.fraunhofer.aisec/cpg-analysis "5.1.0"]
                 [org.jetbrains.kotlin/kotlin-runtime "1.2.71"]
                 ]
  :repl-options {:init-ns cpg-test.core}
  :resource-paths ["local-repo/core-7.2.100.202105180159.jar"]
  )
