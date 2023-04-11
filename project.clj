(defproject cpg-test "1.0.1"
    :description "Analyser for Java 11 Projects"
    :url "https://github.com/MaximilianLincks/cpg-test"
    :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
              :url  "https://www.eclipse.org/legal/epl-2.0/"}
    :dependencies [[org.clojure/clojure "1.11.1"]
                   [de.fraunhofer.aisec/cpg-core "5.1.0"]
                   [de.fraunhofer.aisec/cpg-analysis "5.1.0"]
                   [org.clojure/data.json "2.4.0"]
                   [org.clojure/math.combinatorics "0.2.0"]
                   [org.clojure/tools.cli "1.0.214"]]
    :main cpg-test.core
    :aot :all
    :repl-options {:init-ns cpg-test.core}
    )
