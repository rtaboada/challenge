# Challenge


[Shortest path problem](http://en.wikipedia.org/wiki/Shortest_path_problem)  
[Floyd-Warshall](http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm)  
[Closeness Centrality](http://en.wikipedia.org/wiki/Centrality#Closeness_centrality)  
[Pedestal](https://github.com/pedestal/pedestal)  

## Usage

`lein run` will start the Pedestal server.

### Endpoints

|  Method  | Endpoint | Description |
|----------|----------|-------------|
| GET | /edge |   Returns a list all edges in the graph. |
| POST | /edge | Create a new edge, expects `vertex1` and `vertex2` as the id (an int) of the vertices. And accepts an optional parameter `undirected` that when true makes an undirected edge, the default is `false`. |
| GET | /edge/\<v1\>/\<v2\> | Retrieve information about the edge. |
| DELETE | /edge/\<v1\>/\<v2\> | Delete the edge from the graph. |
| GET | /vertex | Return a list of all vertices. |
| GET | /vertex/score | Returns the current score of every vertex, sorted from the highest to lowest score. |
| GET | /vertex/\<vertex-id\> | Return information about the vertex with vertex-id. |
| PUT | /vertex/\<vertex-id\>/fraudulent | Flags a vertex as fraudulent. |


## License

Copyright © 2015 Rodrigo Taboada

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
