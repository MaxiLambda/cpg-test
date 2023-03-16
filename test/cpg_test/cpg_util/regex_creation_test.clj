(ns cpg-test.cpg-util.regex-creation-test
  (:require [clojure.test :refer :all])
  (:require [cpg-test.cpg-util.regex-creation :refer [regex-partial-match]]))

(deftest regex-partial-match-test
(is (= (regex-partial-match "de\\.root\\.Maxi") "^(de(\\.(root(\\.(Maxi)?)?)?)?)?$"))
(is (= (regex-partial-match "de\\..*\\.Maxi") "^(de(\\.((.*((\\.(Maxi)?)?)?)?)?)?)?$"))
(is (= (regex-partial-match "de.*Maxi") "^(de(.*(Maxi)?)?)?$"))
(is (= (regex-partial-match ".*") "^((.*)?)?$" ))
(is (= (regex-partial-match "de\\..*") "^(de(\\.((.*)?)?)?)?$"))
  )
