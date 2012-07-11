(defn new-user[login password]
  {:user login :password password})

(def accounts [(new-user "captain" "foo"),
               (new-user "dum" "bar")
               ])

(defn process[user]
  {:user user :result 42})

(defn persist-result[result]
  (println (str "Persisted: " result)))

(defn chain[item]
  (->> item 
     (process)
     (persist-result)))

(pmap chain accounts)