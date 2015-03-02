(ns challenge.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [challenge.graph :as graph]))

(defonce edges (atom []))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))


(defn create-edge
  "Adds edge to graph."
  [request]
  (let [v1 (Integer/parseInt (get-in request [:form-params "vertex1"]))
        v2 (Integer/parseInt (get-in request [:form-params "vertex2"]))]
    (swap! edges conj [v1 v2])
    (ring-resp/response (url-for ::view-edge :params {:v1 v1 :v2 v2}))))


(defn list-edges
  "List all edges."
  [request]
  (-> (ring-resp/response @edges)
      (ring-resp/content-type "text/html")))


(defn view-edge
  [request]
  (ring-resp/response "edge"))


(defn delete-edge
  [request]
  (ring-resp/response "deleted"))


(defn list-vertices
  [request]
  (-> (ring-resp/response (graph/vertices @edges))
      (ring-resp/content-type "text/html")))


(defn vertices-score
  "Returns the rank of vertices by closeness centrality."
  [request]
  (ring-resp/response "rank by centrality"))


(defn view-vertex
  [request]
  (let [id (get-in request [:path-params :vertex-id])]
    (ring-resp/response (str "vertex info " id))))


(defn flag-fraudulent
  "Marks a vector as fraudulent."
  [request]
  (ring-resp/response "fraudulent vertex."))


(defroutes routes
  [[["/" {:get home-page}
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
