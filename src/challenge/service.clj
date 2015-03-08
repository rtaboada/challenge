(ns challenge.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [challenge.graph :as graph]
            [challenge.core :as core]))


;; Edges is an atom that keeps the set that contains the graph represented as a set of edges.
(defonce edges (atom #{}))
;; Fraudulents keeps the set of vertices that are currently flagged as fraudulent.
(defonce fraudulents (atom #{}))

(declare url-for)


(defn parse-int 
  "Gets an item in the request path. And parse it to int."
  [request path]
  (Integer/parseInt (get-in request path)))


(defn create-edge
  "Adds edge to graph. Expects two form params: 'vertex1' and 'vertex2'. 
  Can also receive a 'undirected' parameter that if truthy will create an undirected edge. "
  [request]
  (let [v1 (parse-int request [:form-params "vertex1"])
        v2 (parse-int request [:form-params "vertex2"])
        undirected? (get-in request [:form-params "undirected"] false)]
    (swap! edges conj [v1 v2]) 
    (when undirected?
      (swap! edges conj [v2 v1]))
    (ring-resp/created (url-for ::view-edge :params {:v1 v1 :v2 v2}))))


(defn list-edges
  "Lists all edges currently on the graph."
  [request]
  (ring-resp/response @edges))


(defn view-edge
  "Shows edge info."
  [request]
  (let [v1 (get-in request [:path-params :v1])
        v2 (get-in request [:path-params :v2])]
    (ring-resp/response 
     (str (url-for ::view-vertex :params {:vertex-id v1}) 
          "\n"
          (url-for ::view-vertex :params {:vertex-id v2})))))


(defn delete-edge
  "Removes edge from graph. Expects two path params: v1 and v2"
  [request]
  (let [v1 (parse-int request [:path-params :v1])
        v2 (parse-int request [:path-params :v2])]    
    (swap! edges disj [v1 v2])
    (-> (ring-resp/response "")
        (ring-resp/status 204))))


(defn list-vertices
  "Show all vertices. TODO: Add link to vertex info."
  [request]
  (ring-resp/response (graph/vertices @edges)))


(defn vertices-score
  "Returns the rank of vertices by closeness centrality."
  [request]  
  (ring-resp/response (core/score @edges @fraudulents)))


(defn view-vertex
  "TODO: Show vertex info."
  [request]
  (let [id (get-in request [:path-params :vertex-id])]
    (ring-resp/response (str "vertex info " id))))


(defn flag-fraudulent
  "Marks a vector as fraudulent."
  [request]
  (let [vertex-id (parse-int request [:path-params :vertex-id])]
    (swap! fraudulents conj vertex-id)
    (-> (ring-resp/response "")
        (ring-resp/status 204))))


;; Routes of the service.
(defroutes routes
  [[["/" 
     ;; Set default interceptors for any path under /    
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/edge" {:get list-edges
               :post create-edge}
      ["/:v1/:v2"
       ^:constraints {:v1 #"\d+" :v2 #"\d+"}
       {:get view-edge
        :delete delete-edge}]]
     ["/vertex" {:get list-vertices}
      ["/score" {:get vertices-score}]
      ["/:vertex-id"
       ^:constraints {:vertex-id #"\d+"}
       {:get view-vertex}
       ["/fraudulent" {:put flag-fraudulent}]]]]]])

(def url-for (route/url-for-routes routes))

;; Consumed by challenge.server/create-server
;; See bootstrap/default-interceptors for additional options you can
;; configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed sources(s):
              ;;
              ;; "http://localhost:8000"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8080})
