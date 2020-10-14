/*
 * Copyright 2020 link-time GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.linkedplanet.lib.graphlib.graphtypes

import com.linkedplanet.lib.graphlib.*

class DirectedGraph<A>(links: List<Edge<A>>) {
    // --------------------
    // Functions that don't require explanation
    // --------------------

    val getEdgeList: () -> List<Edge<A>> = { links.toSet().toList() }

    fun getVertexList(): List<A> = extractVertexList()

    fun getVerticesCount(): Int = getVertexList().size

    fun getEdgeCount(): Int = getEdgeList().size

    // --------------------
    // Basic Operators
    // --------------------
    operator fun plus(b: DirectedGraph<A>): DirectedGraph<A> =
            DirectedGraph(this.getEdgeList() + b.getEdgeList())

    /* Three Kinds of minus can be defined on a digraph:
     * 1. Subtract a graph from a graph: G1 - G2 
     * 2. Subtract a list of Edges from a graph: G1 - [E]
     * 3. Subtract a list of Vertices from a graph: G1 - [V]
     * 
     * Since this is not a multigraph, we can get away with converting to set
     */
    operator fun minus(b: DirectedGraph<A>): DirectedGraph<A> =
            DirectedGraph((this.getEdgeList().toSet() - b.getEdgeList().toSet()).toList())

    // Sadly JVM can't handle true generics so we have to cheat a little
    // by requiring the edges to be given as a set instead of a list.
    // This Way JVM can differentiate between Option 2 and 3.
    operator fun minus(b: Set<Edge<A>>): DirectedGraph<A> =
            DirectedGraph((this.getEdgeList().toSet() - b).toList())

    operator fun minus(b: List<A>): DirectedGraph<A> =
            DirectedGraph(this.getEdgeList().filterNot { e ->
                b.contains(e.first) || b.contains(e.second)
            }
            )


    // --------------------
    // Functions that may require explanation
    // --------------------

    /**
     * Function constructs a forward directed Map, which maps all Vertices to their out-going Edges
     *
     * Example:
     * [v0] -> Some [(v0,v5),(v0,v4)]
     * [v1] -> Nothing
     * [v2] -> Some [(v2,v5)]
     * ...
     * [vn] -> ...
     *
     * From this map, all terminating nodes (No outbound connections) of the graph are easily found.
     */
    fun getAdjacencyMap(): Map<A, List<Edge<A>>> =
            buildAdjacencyMap(getVertexList(), getEdgeList()) { a -> { b -> b.first == a } }

    /**
     * Function constructs a backwards directed Map, which maps all Vertices to their in-going Edges
     *
     * Example:
     * [v0] -> Some [(v8,v0),(v1,v0)]
     * [v1] -> Nothing
     * [v2] -> Some [(v9,v2)]
     * ...
     * [vn] -> ...
     *
     * From this map, all starting nodes (No inbound connections) of the graph are easily found.
     */
    fun getInverseAdjacencyMap(): Map<A, List<Edge<A>>> =
            buildAdjacencyMap(getVertexList(), getEdgeList()) { a -> { b -> b.second == a } }

    /*
    Thoughts on Cycle detection

    Method A:
    1. Find a Node with only outgoing Edges, If there are no such Nodes, there is a cycle.
    2. Take graph, remove all nodes that have only outgoing nodes including their edges recurse on the new graph until irreducible.
    3. If Elements remain, there is a loop

    Method B:
    1. Same as above
    2. Start a DFS at that Node
    3. When traversing, check if an edge points back to a node on the list of visited nodes; if there are none, there is no cycle

    Another Option:
    If The graph has a Topological Sort, there are no cycles
     */

    /**
     * Function returns true if the graph contains a cycle
     */
    fun hasCycle(): Boolean {
        return this.isCycling()
    }

    /**
     * Function returns a list of trees that are contained within the graph.
     * To achieve this, it uses the asTree function with all current starting nodes
     */
    fun getContainedTrees() =
            getInverseAdjacencyMap().filter { a -> a.value.isEmpty() }
                    .map { vertex -> asTree(vertex.key) }

    /**
     * Returns true if there is a path between the given Vertices in the graph
     */
    fun pathExists(fromVertex: A, toVertex: A): Boolean =
            getPath(fromVertex, toVertex) != null

    /**
     * Returns a path between the given Vertices on the graph or None, if there is no such path.
     */
    fun getPath(fromVertex: A, toVertex: A): List<Edge<A>>? =
            when {
                fromVertex == toVertex -> emptyList()
                this.getEdgeList().contains(Edge(fromVertex, toVertex)) ->
                    listOf(Edge(fromVertex, toVertex))
                this.getVertexList().containsAll(listOf(fromVertex, toVertex)) ->
                    this.getEdgeList().filter { a -> a.first == fromVertex }
                            .map { v -> this.getPath(v.second, toVertex)?.map { a -> listOf(v) + a } }
                            .foldRight(null as List<Edge<A>>?) { a: List<List<Edge<A>>>?, acc: List<Edge<A>>? ->
                                a?.flatten() ?: acc
                            }
                else -> null
            }

    /**
     * Function generates a tree from the graph with the given startingNode as Tree-root
     */
    private fun asTree(startingVertex: A): Tree<A>? =
            if (this.getVertexList().contains(startingVertex)) {
                val reducedGraph = DirectedGraph(this.removeDisconnectedGraphs(startingVertex).getEdgeList())
                val startingVertices = DirectedGraph(reducedGraph.getEdgeList().filter { a -> a.first != startingVertex }).getInverseAdjacencyMap()
                        .filter { a -> a.value.isEmpty() }
                        .map { a -> a.key }
                val endingVertices = reducedGraph.getVertexList().filterNot { it == startingVertex }

                Tree(startingVertex,
                        (if (startingVertices.isNotEmpty()) {
                            startingVertices.mapNotNull { a -> DirectedGraph(reducedGraph.getEdgeList().filter { b -> b.first != startingVertex }).asTree(a) }
                        } else {
                            endingVertices.map { Tree(it, null) }
                        }))
            } else {
                null
            }

    /**
     * Function returns a Directed Graph that is strongly connected with the given reference Vertex
     */
    private fun removeDisconnectedGraphs(referenceVertex: A): DirectedGraph<A> =
            this - (this.getVertexList()
                    .filterNot { v ->
                        pathExists(referenceVertex, v) || pathExists(v, referenceVertex)
                    })

    /**
     * Function takes a list of vertices and a list of edges as well as
     */
    private fun buildAdjacencyMap(
            vList: List<A>,
            eList: List<Edge<A>>,
            matcher: (A) -> Predicate<Edge<A>>
    ): Map<A, List<Edge<A>>> =
            vList.map { vertex ->
                Pair(vertex,
                        eList.filter { a -> matcher(vertex)(a) })
            }.toMap()

    /**
     * Function takes a list of Edges and returns the list of Vertices connected by them.
     */
    private fun extractVertexList(): List<A> =
            getEdgeList()
                    .map {
                        listOf(it.first, it.second)
                    }.flatten()
                    .toSet()
                    .toList()


    /**
     * Function takes a graph and recursively reduces it by all Nodes that can't be part of a cycle.
     * If during reduction a graph is produced, which can't be reduced further and has more than one Element,
     * that graph is a cycle.
     */
    private fun isCycling(): Boolean {
        return when (this.getVerticesCount()) {
            0 -> false
            else -> this.reduceByStartingNodes()?.isCycling() ?: true
        }
    }

    /**
     * Function reduces a directed graph by removing all Nodes, which don't have any inbound Edges
     * Before returning, it checks whether any Nodes have been removed.
     * If this is not the case it returns None, indicating the graph is irreducible.
     */
    private fun reduceByStartingNodes(): DirectedGraph<A>? {
        val reducedGraph = this - (this.getInverseAdjacencyMap()
                .filterNot { a -> a.value.isNotEmpty() }
                .keys
                .toList())
        return if (reducedGraph.getVerticesCount() == this.getVerticesCount()) {
            null
        } else {
            reducedGraph
        }
    }
}
