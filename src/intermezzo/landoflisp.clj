(ns intermezzo.landoflisp)

; Guess my number

(def *upper-bound* (atom 100))
(def *lower-bound* (atom 1))

(defn guess-my-number[]
  (int (/ (+ @*upper-bound* @*lower-bound*) 2)))

(defn smaller[]
  (reset! *upper-bound* (- (guess-my-number) 1))
  (guess-my-number))

(defn bigger[]
  (reset! *lower-bound* (+ (guess-my-number) 1))
  (guess-my-number))

(defn reset[]
  (def *upper-bound* (atom 100))
  (def *lower-bound* (atom 1)))