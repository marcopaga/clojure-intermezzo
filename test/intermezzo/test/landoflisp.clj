(ns intermezzo.test.landoflisp
  (:use [intermezzo.landoflisp])
  (:use [clojure.test]))


(deftest guess-my-number-test
  (is (= 50 (guess-my-number)))
  (is (= 25 (smaller)))
  (is (= 37 (bigger)))
  (is (= 31 (smaller)))
  (is (= 28 (smaller)))
  (is (= 29 (bigger))))