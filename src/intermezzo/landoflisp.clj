(ns intermezzo.landoflisp)

; Guess my number

(def *upper-bound* 100)

(def *lower-bound* 1)

(defn guess-my-number[]
  (int (/ (+ *upper-bound* *lower-bound*) 2)))

(defn smaller[]
  (def *upper-bound* (- (guess-my-number) 1))
  (guess-my-number))

(defn bigger[]
  (def *lower-bound* (+ (guess-my-number) 1))
  (guess-my-number))

(defn reset[]
  (def *upper-bound* 100)
  (def *lower-bound* 1))