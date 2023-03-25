(ns cpg-test.cpg-util.regex-creation-test
    (:require [clojure.test :refer :all]
              [cpg-test.cpg-util.regex-creation :refer [to-regex]]))

(deftest finish-regex-test
    (is (= (to-regex "{}" {}) ".*"))
    (is (= (to-regex "a$b" {}) "a\\$b"))
    (is (= (to-regex "abc.{}de" {}) "abc\\..*de"))
    (is (= (to-regex "a.{test}.c" {"{test}" "b"}) "a\\.b\\.c"))
    (is (= (to-regex "{test}" {}) ".*"))
    (is (= (to-regex "{test}" {"{test}" "{test2}" "{test2}" "abc"}) "abc"))
    (is (= (to-regex "a{test}" {"{test}" "{test}"}) "a.*"))
    )

