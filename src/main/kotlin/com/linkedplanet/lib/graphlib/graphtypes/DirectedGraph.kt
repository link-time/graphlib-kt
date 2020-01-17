package com.linkedplanet.lib.graphlib.graphtypes

import arrow.core.*
import arrow.core.extensions.list.traverse.map
import com.linkedplanet.lib.graphlib.Edge
import com.linkedplanet.lib.graphlib.id
import com.linkedplanet.lib.graphlib.reduceEdgeList


class DirectedGraph<A>(links: List<Edge<A>>) {
    val getEdgeList: () -> List<Edge<A>> = { links.toSet().toList() }

    fun getVertexList(): List<A> =
            extractNodeList().toSet().toList()

    fun getVerticesCount(): Int = getVertexList().size

    fun getEdgeCount(): Int = getEdgeList().size

    /***
     * getAdjacencyMap
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

    /***
     * getInverseAdjacencyMap
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
     * hasCycle
     *
     * returns true if the graph contains a cycle
     */
    fun hasCycle(): Boolean {
        return this.isCycling().fold({ true }, { x -> x.id() })
    }

    /**
     * buildAdjacencyMap
     *
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

    private fun extractNodeList(): List<A> =
            getEdgeList()
                    .map {
                        listOf(it.a, it.b)
                    }.flatten()


    /***
     * isCycling
     *
     * Function takes a graph and recursively reduces it by all Nodes that can't be part of a cycle.
     * If during reduction a graph is produced, which can't be reduced further and has more than one Element,
     * that graph is a cycle.
     */
    private fun isCycling(): Option<Boolean> {
        return when (this.getVerticesCount()) {
            0 -> Some(false)
            else -> this.reduceByStartingNodes().fold({ None }, { it.isCycling()})
        }
    }

    /***
     * reduceByStartingNodes
     *
     * Function reduces a directed graph by removing all Nodes, which don't have any inbound Edges
     * Before returning it checks whether any Nodes have been removed,
     * if not it returns None, indicating the graph is irreducible.
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

