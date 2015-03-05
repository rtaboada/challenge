# Challenge

The problem is to find the score of every vertex in a graph. The initial score is simply the [Closeness Centrality](http://en.wikipedia.org/wiki/Centrality#Closeness_centrality) of the vertex. But a vertex can be flagged as fraudulent which would reduce the vertex score to zero and reduce the score of every other vertex by a factor proportional to the distance between it and the fraudulent vertex.

To calculate the _Closeness Centrality_ we need to solve the [shortest path problem](http://en.wikipedia.org/wiki/Shortest_path_problem) for the graph. The algorithm I choose to resolve the problem was the [Floyd-Warshall](http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm) algorithm. This algorithm can handle both positive and negative weights and is kind of easy to express in a functional language because of the recurrence formula:

    shortestPath(i, j, 0) = w(i, j)
    shortestPath(i, j, k+1) = min(shortestPath(i, j, k), 
                                  shortestPath(i, k+1, k) + shortestPath(k+1, j, k)) 

This formula makes the algorithm a good candidate for using a technique called [memoization](http://en.wikipedia.org/wiki/Memoization). In Clojure you need only to wrap your function with the [`memoize`](https://clojuredocs.org/clojure.core/memoize) function and the result of every call to your original function will be saved. There is also a library [core.memoize](https://github.com/clojure/core.memoize) that gives a lot more control over the cache of results but I didn't explored it further.

I keep the state of the application in two `atom`s, the edges `atom` keeps a `set` of edges that are currently in the graph, and the fraudulents `atom` keeps a `set` of all vertices that are flagged as fraudulents. The `calculate-score` function uses this two `atoms` to calculate the current score of the vertices in the graph. Because of the use of memoization there is no need to have a separated score `atom`. There are some downsides: every client see the same graph, the graphs are not durable (i.e. they vanish when the server terminates). I select this implementation only because of its simplicity, if that is unacceptable I will gladly change the code and use Datomic to save the graphs.

#### Assumptions: 

- The graph is connected.
- The vertex id is always an `Integer`.
- The first vertex id is zero and the last vertex id is `(dec (count (vertices)))`.
- The ids are compact, i.e. without holes.

### Libraries used:

- [Pedestal](https://github.com/pedestal/pedestal): Used to implement the [RESTful](http://en.wikipedia.org/wiki/Representational_state_transfer) web server
- [test.check](https://github.com/clojure/test.check): Use to implement property based tests. There are two great presentations at Clojure/West 2014 about this kind of test [John Hughes - Testing the Hard Stuff and Staying Sane](https://www.youtube.com/watch?v=zi0rHwfiX1Q) and [Reid Draper - Powerful Testing with test.check](https://www.youtube.com/watch?v=JMhNINPo__g).

## Usage

- To run the server: in the project directory `lein run` starts the Pedestal server.
- Use `lein test` in the project directory to run all unit and property-based tests.

### Endpoints

|  Method  |     Endpoint   | Description |
|----------|----------------|-------------|
| GET | /edge        |   Returns a list all edges in the graph. |
| POST | /edge | Creates a new edge, expects `vertex1` and `vertex2` as the id (an int) of the vertices. And accepts an optional parameter `undirected` that when true makes an undirected edge, the default is `false`. |
| GET | /edge/\<id1\>/\<id2\> | Retrieves information about the edge. |
| DELETE | /edge/\<id1\>/\<id2\> | Deletes the edge from the graph. |
| GET | /vertex | Returns a list of all vertices. |
| GET | /vertex/score   | Returns the current score of every vertex, sorted from the highest to lowest score. |
| GET | /vertex/\<id\>   | Returns information about the vertex with vertex-id. |
| PUT | /vertex/\<id\>/fraudulent      | Flags a vertex as fraudulent. |


## License

Copyright © 2015 Rodrigo Taboada

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
