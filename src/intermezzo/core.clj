(ns intermezzo.core
  (:import (java.text SimpleDateFormat)
           (java.util Calendar TimeZone Date)
           (java.text SimpleDateFormat)))

(def visitors (ref #{}))

(defn hello
  [username]
  (dosync
    (let [visited (@visitors username)]
      (if visited
        (str "Hallo zurück, " username)
        (do
          (alter visitors conj username)
          (str "Hallo, " username))))))

(defn basic-item-total
  ([price quantity]
    (* price quantity))
  ([price]
    (basic-item-total price 1)))

(defn with-line-item-conditions [f price quantity]
  {:pre [(> price 0) (> quantity 0)]
   :post [(> % 1)]}
  (apply f [price quantity]))

(def item-total                                            ;def != defn
  (partial with-line-item-conditions basic-item-total))    ;partial keeps some paramters fixed. Additional params are added on call

(defn my-sum
  [& numbers]
  {:post [(< 0 %)]}
  (reduce + numbers))

(defn count-down [n]
  (if-not (zero? n)
    (do
      (if (= 0 (rem n 100))
        (println "count-down:" n))
      (recur (dec n)))))                  ; self recursive call (stack is preserved)

(declare trampoline-two)

(defn trampoline-one
  [value]
  (if-not (<= value 0)
    (do
      (if (= 0 (rem value 100))
        (println "Value: " value))
      #(trampoline-two (dec value)))))    ; #() is a function definition

(defn trampoline-two
  [value]
  #(trampoline-one (dec value)))

(defn do-trampolining
  []
  (trampoline trampoline-one 100000))

(defn my-slow-sum
  [& numbers]
  (println "slow sum called")
  (reduce + numbers))

(def my-fast-sum
  (memoize my-slow-sum))

;---

(def users [
             {:username "kyle"
              :balance 175.00
              :member-since "2009-04-16"}
             {:username "zak"
              :balance 12.95
              :member-since "2009-02-01"}
             {:username "rob"
              :balance 98.50
              :member-since "2009-03-30"}
             ])

(defn username [user]
  (user :username))

(defn balance [user]
  (user :balance))

(defn sorter-using [ordering-fn]
  (fn [users]
    (sort-by ordering-fn users)))

(def poorest-first (sorter-using balance))

(def alphabetically (sorter-using username))

;-- anonymous functions

(defn user-join-dates-long
  []
  (map (fn [user] (user :member-since)) users))

(defn user-join-dates
  []
  (map #(% :member-since) users))  ;%: first argument

; Special vars and binding
; Action at a distance

(def ^:dynamic *name* "anna")

(defn print-name
  []
  (println *name*))

(defn change-name-before-print
  []
  (binding [*name* "Anna"]
    (print-name)))

; Dynamic scope
; depending on the execution path

(def ^:dynamic *eval-me* 10)
(defn print-the-var [label]
  (println label *eval-me*))

(print-the-var "A:")
(binding [*eval-me* 20] ;; the first binding
  (print-the-var "B:")
  (binding [*eval-me* 30] ;; the second binding
    (print-the-var "C:"))
  (print-the-var "D:"))
(print-the-var "E:")

; "aop"  with higher order functions and bindings

(defn hello-you
  [name]
  (println (str "Hello, " name "!")))

(defn with-logging
  [target-function]
  (fn [& args]                                    ;this anonymous function is returned when called
    (println (str "Calling: " target-function))
    (apply target-function args)
    ))

; (binding [call-hello-you (with-logging call-hello-you)]
;  (call-hello-you "Marco"))


; let form (local variables)

(defn let-with-functions
  []
  (let [upper-case #(.toUpperCase %)]
    (println (upper-case "marco"))))

;Destructuring

(defn map-destructure
  [{first :first last :last middle :middle :or {middle "<no middle name>"}}]   ;destructuring of incoming map (similar to pattern matching)
  (println (str "First: " first " Middle: " middle " Last: " last)))

(defn list-destructure
  [[first second & rest]]
  (println (str "First: " first " second: " second " and: " rest)))

(defn call-destructure []
  (map-destructure {:first "Anna" :last "Michel"})
  (map-destructure {:first "Marco" :middle "Klaus" :last "Paga"})
  (list-destructure ["Marco" "Paga" "Even" "More"]))

; meta-data

(def untrusted (with-meta {:command "clean-table" :subject "users"}
                 {:author "Marco Paga"}))

(defn print-meta-data
  "Prints the meta data"
  {:author "Marco Paga"}
  [data]
(let [meta-data (meta data)]
  (println (str "Data: " data " Meta-Data: " meta-data))))

(defn print-meta-of-function
  []
  (println (meta (var print-meta-data))))

; multi-methods

(defmulti i18n-greeting :language)

(defmethod i18n-greeting :german
  [person]
  (println (str "Hallo, " (person :name) ". Wie geht es Dir?")))

(defmethod i18n-greeting :english
  [person]
  (println (str "Hello, " (person :name) ". How are you?")))

(def german-guy {:name "Karl" :language :german})
(def english-guy {:name "Steven" :language :english})

(defn test-multimethods []
  (i18n-greeting german-guy )
  (i18n-greeting english-guy ))


(defn launcher-category [object]
  [(object :type) ::cheap])

(defmulti launcher launcher-category)     ;dispatching function. Param: Name of the function to call on object

(defmethod launcher [:rocket ::cheap]   ;the result of the above dispatching call as the argument
  [rocket]
  (println (str "Launching rocket: ", rocket)))

(defmethod launcher [:toaster ::cheap]
  [toaster]
  (println (str "Starting toaster: ", toaster)))

(defmethod launcher :default [object]
  (println (str "Default case. Nothing launched here... Object: " object)))

(def rocket {:type :rocket :name "No.1" })
(def toaster{:type :toaster :name "Krups Cheap Toaster"})

(defn test-launcher-multimethod []
  (launcher rocket)
  (launcher toaster))

; hierachies

(derive ::bronze ::basic)
(derive ::silver ::basic)
(derive ::gold ::premier)
(derive ::platinum ::premier)
(derive ::mdb ::basic)
(derive ::mdb ::premier)

(defmulti mileage-plus :status)

(defmethod mileage-plus ::basic
  [basic-flyer]
  (println (str "Basic kind flyer: "basic-flyer)))

(defmethod mileage-plus ::platinum
  [platinum-flyer]
  (println (str "Platinum flyer: ", platinum-flyer)))

(defmethod mileage-plus ::premier
  [premier-kind-flyer]
  (println (str "Premier kind flyer: ", premier-kind-flyer)))

(prefer-method mileage-plus ::basic ::premier)

(defn test-mileage-plus []
  (mileage-plus {:name "Platinum" :status ::platinum})
  (mileage-plus {:name "Gold" :status ::gold})
  (mileage-plus {:name "Silver" :status ::silver})
  (mileage-plus {:name "Bronze" :status ::bronze})
  (mileage-plus {:name "Mitglied des Bundestags" :status ::mdb}))

; java interop
(def my-sdf (SimpleDateFormat. "dd.MM.yyyy"))

(defn date-from-string [string]
  (let [sdf (SimpleDateFormat. "dd.MM.yyyy")]
    (.parse sdf string)))

(defn last-midnight []
  (let [calendar (Calendar/getInstance)]
    (doto calendar
      (.set Calendar/AM_PM Calendar/AM)
      (.set Calendar/HOUR 0)
      (.set Calendar/MINUTE 0)
      (.set Calendar/SECOND 0)
      (.set Calendar/MILLISECOND 0))
    (.getTime calendar)))

(defn test-java-interop []
  (println (date-from-string "15.11.1982"))
  (println (str "Long.parseLong(\"1212\") : " (Long/parseLong "1212")))   ; preferred
  (println (str "Long.parseLong(\"1212\") : " (. Long parseLong "1212"))) ; . means in the conext of
  (println (str "Calendar.JANUARY : " (Calendar/JANUARY) " and with the . operator: " (. Calendar JANUARY)))
  (println (str "System.getenv : " (. System getenv "PATH")))
  (println (str "Current Time zone with the .. operator: "
             (.. (Calendar/getInstance) getTimeZone (getDisplayName true TimeZone/SHORT))))
  (println (str "last midnight: " (last-midnight)))
  (println (str "bean function: " (bean (last-midnight))))
  )

(defn test-memfn []
  (println (str "Simple method call via anonymous function: " (map #(.getBytes %) ["marco" "anna"])))
  (println (str "MEMber FuNction operator: "(map (memfn getBytes) ["marco" "anna"])))
  (println (str "Member function with method parameters: " ((memfn subSequence start end) "Marco" 2 4)))
  )

(defn test-proxy []
  (let [runnable (proxy [Runnable] []
    (run []
      (println "Run was called")))]
    (. runnable run)))

;           Concurrency

;refs with alter

(def all-users (ref {}))

(defn new-user [id login monthly-budget]
  {:id id
   :login login
   :monthly-budget monthly-budget
   :total-expenses 0})

(defn add-new-user [login budget-amount]
  (dosync
    (let [current-number (count @all-users)
          user (new-user (inc current-number) login budget-amount)]
      (alter all-users assoc login user))))


; refs with commute (+ 1 is commutative)
(def my-counter (ref 0))

(defn test-my-counter []
  (dosync (commute my-counter inc))
  (dosync (commute my-counter + 10)))

; agents
(defn test-agent-counter []
  (let [agent-counter (agent 0 :validator #(>= % 0))]
    (println @agent-counter)
    (send-off agent-counter inc) ; can handle potential blocking actions
    (println @agent-counter)
    (send agent-counter inc) ;should be used for non blocking actions (fixed thread pool will be used)
    (println @agent-counter)
    (send agent-counter + 40)
    (await-for 1000 agent-counter)
    (println @agent-counter)))

(defn test-agent-errors []
  (let [dead-agent (agent 10)]
    (send dead-agent / 0)       ; kill the agent
    (await-for 1000 dead-agent) ; wait for the async call
    (println (.printStackTrace (first (agent-errors dead-agent)))) ;check for the Exception
    (clear-agent-errors dead-agent) ; bring it to life again
    (println (@dead-agent))))

;atoms
(defn test-atoms []
  (let [my-atom (atom 0)]
    (swap! my-atom + 10)              ;simply execute the function on the atom
    (compare-and-set! my-atom 10 42)  ;set the value of the atom if it is "now" 10
    (println @my-atom)
    (reset! my-atom 17)               ;set thee value of the atom to 17
    (println @my-atom)))

                                        ; guarnteed-unique-variable#
                                        ; ~quoted-variable

                                        ;--- snip

(defn on-change [key ref old-value new-value]
  (println "Value changed from " old-value " to " new-value  " <" key " - " ref  " >" ))

(defn test-event-listener []
  (let [my-atom (atom 0)]
    (add-watch my-atom :my-atom-watcher on-change)
    (compare-and-set! my-atom 0 1)
    (println @my-atom)
    (remove-watch my-atom :my-atom-watcher)
    (compare-and-set! my-atom 1 2)
    (println @my-atom)))

(test-event-listener)

(defn long-running [x y]
  (Thread/sleep 500)
  (+ x y))

(defn test-futures []
  (let [future-one (future (long-running 17 42))
        future-two (future (long-running 19 47))]
    (println "Future 1: " @future-one " Future 2: " @future-two)))

(time (test-futures))

(defn test-promises []
  (let [my-promise (promise)
        my-agent (agent 0)]
    (future (deliver my-promise 12))
    (println "The value of the promise is: " @my-promise)))

(test-promises)

                                        ; macros

(defmacro sync-set [reference value]
  (list 'dosync
        (list 'ref-set reference value)))

(defn test-sync-set-macro []
  (let [my-ref (ref 0)]
    (sync-set my-ref 42)
    (println "MyRef is now " @my-ref)))

(test-sync-set-macro)

; works but somehow hard to read and maintain
;(defmacro unless [test then]
;  (list 'if (list 'not test)
;        then))

(defmacro unless [test & expressions]
  `(if (not ~test)            ; ` back-quote : start macro template. ~ is used to quote vars
     (do ~@expressions)))     ; @ to splice the expressions into the wrapping list


(defn test-unless []
  (unless (even? 11)
          (println "it is odd...")
          (println "really damn odd!")))

(test-unless)

             ; macro gensym
(defmacro def-logged-fn [fn-name args & body]
  `(defn ~fn-name ~args
     (let [now# (System/currentTimeMillis)]
       (println "[" now# "] Call to" (str (var ~fn-name)))
       ~@body)))

(def-logged-fn logged-greet [username]
  (println "Hallo " username))

(logged-greet "Marco")

(defn traced-call [value]
  (println "Call to " "traced-call")
  (time (+ 1 2)))

(traced-call "val")

(defmacro def-traced [fn-name args & body]
  `(defn ~fn-name ~args
     (println "Call to " ~fn-name)
     (time ~@body)))

(def-traced add-values [x y]
  (+ x y))

(add-values 15 27)

  (defmacro randomly [& exprs]
          (let [len (count exprs)
                index (rand-int len)
                conditions (map #(list '= index %) (range len))]
            `(cond ~@(interleave conditions exprs))))

(defmacro randomly-2 [& exprs]                  ;Cavecat: What happens when called from within the body of a function definition?
          (nth exprs (rand-int (count exprs))))

(defn test-randomly-2 []
(randomly-2
 (println "amit")
 (println "deepthi")
 (println "adi")))

(test-randomly-2)

(def request {:username "amit" :password "123456"})

(defmacro defwebmethod [name args & exprs]
  `(defn ~name [{:keys ~args}]
     ~@exprs))

(defn check-credentials [username password]
  true)

(defwebmethod login-user [username password]
  (if (check-credentials username password)
    (str "Welcome, " username ", " password " is still correct!")
    (str "Login failed!")))

(login-user request)

(defn date [date-string]
  (let [date-format (SimpleDateFormat. "yyyy-MM-dd")]
    (.parse date-format date-string)))

(defn day-from [date]
  (:date (bean date)))

(defn month-from [date]
  (inc (:month (bean date))))


                                        ;--- snip end

(defmacro assert-true [expression]
  (let [[operator lhs rhs] expression]
    `(let [lhsv# ~lhs
          rhsv# ~rhs
          ret# ~expression]
    (if-not ret#
      (throw (IllegalArgumentException. (str ~lhs " is not " ~operator " " ~rhs)))
      true))))


(assert-true (= (+ 1 2) (- 6 3)))

                                        ; TDD

(defstruct expense :amount :date)

(defn log-call [id & args]
  (println "Audit log: " id " - " args))

(defn ^:dynamic fetch-all-expenses [username start-date end-date]
  (log-call "fetch-all" username start-date end-date)
  (list (struct expense 12.99 (Date.)) (struct expense 27.99 (Date.))))

(defn fetch-expenses-greater-than [username start-date end-date threshold]
  (log-call "fetch-expenses-greater-than" username start-date end-date threshold)
  (let [all (fetch-all-expenses username start-date end-date)]
    (filter #(> (:amount %) threshold) all)))

(defn parse-line [line]
  (let [tokens (.split (.toLowerCase line) " ")]
    (map #(vector % 1) tokens)))

(defn combine [mapped]
  (->> (group-by first mapped)
       (map (fn [[k v]]
              {k (map second v)}))
       ))

(defn sum [[k v]]
  {k (apply + v)})

(defn threaded-count-words [words]
  (->> (parse-line words)
       (combine)
       (apply merge-with conj)
       (map sum)))
