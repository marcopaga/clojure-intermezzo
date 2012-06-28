(defproject intermezzo "1.0.0-SNAPSHOT"
  :description "intermezzo of some Clojure"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [ring "1.0.1"]
                 [compojure "1.0.1"]]
  :dev-dependencies [[lein-ring "0.5.4"]]
  :ring {:handler intermezzo.ring/handler})
