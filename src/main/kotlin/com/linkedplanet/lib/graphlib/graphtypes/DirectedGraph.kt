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

import arrow.core.*
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.extensions.list.traverse.map
import arrow.core.extensions.list.traverse.sequence
import arrow.core.extensions.option.alternative.orElse
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.applicative.map
import com.linkedplanet.lib.graphlib.*


class DirectedGraph<A>(links: List<Edge<A>>) {
    val getEdgeList: () -> List<Edge<A>> = { links.toSet().toList() }

    fun getVertexList(): List<A> = extractVertexList()

    fun getVerticesCount(): Int = getVertexList().size

    fun getEdgeCount(): Int = getEdgeList().size

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
            buildAdjacencyMap(getVertexList(), getEdgeList()) { a -> { b -> b.a == a } }

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
            buildAdjacencyMap(getVertexList(), getEdgeList()) { a -> { b -> b.b == a } }

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
        return this.isCycling().fold({ true }, { x -> x.id() })
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
            getPath(fromVertex, toVertex) is Some

    /**
     * Returns a path between the given Vertices on the graph or None, if there is no such path.
     */
    fun getPath(fromVertex: A, toVertex: A): Option<List<Edge<A>>> =
            when {
                fromVertex == toVertex -> Some(emptyList())
                this.getEdgeList().contains(Edge(fromVertex, toVertex)) ->
                    Some(listOf(Edge(fromVertex, toVertex)))
                this.getVertexList().containsAll(listOf(fromVertex, toVertex)) ->
                    this.getEdgeList().filter { a -> a.a == fromVertex }
                            .map { v -> this.getPath(v.b, toVertex).map { a -> listOf(v) + a } }
                            .foldRight(None) { a: Option<List<Edge<A>>>, b: Option<List<Edge<A>>> -> a.orElse(b) }
                            .flatMap {
                                when (it) {
                                    emptyList<Edge<A>>() -> None
                                    else -> Some(it)
                                }
                            }
                else -> None
            }

    /**
     * Function generates a tree from the graph with the given startingNode as Tree-root
     */
    private fun asTree(startingVertex: A): Option<Tree<A>> =
            if (this.getVertexList().contains(startingVertex)) {
                val reducedGraph = DirectedGraph(this.removeDisconnectedGraphs(startingVertex).getEdgeList())
                val startingVertices = DirectedGraph(reducedGraph.getEdgeList().filter { a -> a.a != startingVertex }).getInverseAdjacencyMap()
                        .filter { a -> a.value.isEmpty() }
                        .map { a -> a.key }
                val endingVertices = reducedGraph.getVertexList().filterNot { it == startingVertex }

                Some(Tree(startingVertex,
                        ( if (startingVertices.isNotEmpty()) {
                            startingVertices.map { a -> DirectedGraph(reducedGraph.getEdgeList().filter { a -> a.a != startingVertex }).asTree(a) }
                                    .sequence(Option.applicative())
                                    .map { it.fix() }
                        } else {
                            Some(endingVertices.map { Tree(it, None)})
                        })))
            } else {
                None
            }

    /**
     * Function returns a Directed Graph that is strongly connected with the given reference Vertex
     */
    private fun removeDisconnectedGraphs(referenceVertex: A): DirectedGraph<A> =
            DirectedGraph(this.getEdgeList().reduceEdgeList(
                    this.getVertexList().filter { v -> pathExists(referenceVertex, v) || pathExists(v, referenceVertex) }
            ))

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
                        eList.filter { a -> Option(a).map(matcher(vertex)).fold({ false }, { it.id() }) })
            }.toMap()

    /**
     * Function takes a list of Edges and returns the list of Vertices connected by them.
     */
    private fun extractVertexList(): List<A> =
            getEdgeList()
                    .map {
                        listOf(it.a, it.b)
                    }.flatten()
                    .toSet()
                    .toList()


    /**
     * Function takes a graph and recursively reduces it by all Nodes that can't be part of a cycle.
     * If during reduction a graph is produced, which can't be reduced further and has more than one Element,
     * that graph is a cycle.
     */
    private fun isCycling(): Option<Boolean> {
        return when (this.getVerticesCount()) {
            0 -> Some(false)
            else -> this.reduceByStartingNodes().fold({ None }, { it.isCycling() })
        }
    }

    /**
     * Function reduces a directed graph by removing all Nodes, which don't have any inbound Edges
     * Before returning, it checks whether any Nodes have been removed.
     * If this is not the case it returns None, indicating the graph is irreducible.
     */
    private fun reduceByStartingNodes(): Option<DirectedGraph<A>> {
        val reducedGraph = DirectedGraph(this.getEdgeList()
                .reduceEdgeList(this.getInverseAdjacencyMap()
                        .filter { a -> a.value.isNotEmpty() }
                        .keys
                        .toList()))
        return if (reducedGraph.getVerticesCount() == this.getVerticesCount()) {
            None
        } else {
            Some(reducedGraph)
        }
    }
}
