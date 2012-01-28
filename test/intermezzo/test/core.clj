(ns intermezzo.test.core
  (:use [intermezzo.core])
  (:use [clojure.test]))

;(deftest hello-test
;  (is (.equals "Hallo, Marco" (hello "Marco")))
;  (is (.equals "Hallo zurück, Marco" (hello "Marco"))))

(deftest item-total-test
  (is (= 20 (item-total 10 2))))

(deftest test-agent
  (let [my-agent (agent 0)]
    (send my-agent inc)
    (await-for 1000 my-agent)
    (is (= 1 @my-agent))))

(defmacro stubbing [stub-forms & body]
  (let [stub-pairs (partition 2 stub-forms)
        returns (map last stub-pairs)
        stub-fns (map #(list 'constantly %) returns)
        real-fns (map first stub-pairs)]
    `(binding [~@(interleave real-fns stub-fns)]
       ~@body)))

(def all-expenses [(struct-map expense :amount 12.99 :date "2010-02-28")
                           (struct-map expense :amount 39.99 :date "2010-02-25")
                           (struct-map expense :amount 9.0 :date "2010-02-21")])

(deftest test-fetch-expenses-greater-than
  (stubbing [fetch-all-expenses all-expenses]
    (let [filtered (fetch-expenses-greater-than "" 1 2 13)]
      (is (= (count filtered) 1))
      (is (= (:amount (first filtered)) 39.99)))))

                                        ; are
(deftest test-are
  (are [string upper] (= upper (.toUpperCase string))
       "string" "STRING"
       "another" "ANOTHER"))