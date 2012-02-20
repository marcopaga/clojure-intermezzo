(ns intermezzo.ring
  (:use ring.adapter.jetty
        ring.middleware.reload
        ring.middleware.stacktrace
        ring.handler.dump
        ring.middleware.cookies
        ring.middleware.session
        ring.middleware.params
        compojure.core
        intermezzo.core)
  (:require [compojure.route :as route]))

(defn what-is-my-ip [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (:remote-addr request)})

(def ring-handler
  (-> what-is-my-ip
    wrap-reload
    wrap-params
    wrap-cookies
    wrap-session
    wrap-stacktrace))

(defroutes main-routes
  (GET "/" [] "<h1>Hello World</h1>")
  (GET "/hello/:name" [name]
       (hello name))
  (route/not-found "<h1>Page not found!</h1>"))

(def handler (wrap-stacktrace main-routes))