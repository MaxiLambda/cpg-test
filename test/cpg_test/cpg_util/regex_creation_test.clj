(ns cpg-test.cpg-util.regex-creation-test
  (:require [clojure.test :refer :all])
  (:require [cpg-test.cpg-util.regex-creation :refer [regex-partial-match]]))

(deftest regex-partial-match-test
(is (= "^(de(\\.(root(\\.(Maxi)?)?)?)?)?$" (regex-partial-match "de\\.root\\.Maxi")))
(is (= "^(de(\\.((.*((\\.(Maxi)?)?)?)?)?)?)?$" (regex-partial-match "de\\..*\\.Maxi")))
(is (= "^(de(.*(Maxi)?)?)?$" (regex-partial-match "de.*Maxi")))
  )
