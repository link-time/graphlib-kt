package com.linkedplanet.lib.graphlib

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.list.traverse.map

/**
 * Universal Identity function
 */
fun <A> A.id(): A = this

/**
 * Reduces the list provided as receiver to a list of edges that connect the edges provided as parameter.
 *
 * Example:
 * [(a,b),(b,c),(c,d)].reduceEdgeList([b,c,d]) => [(b,c),(c,d)]
 */
fun <A> List<Edge<A>>.reduceEdgeList(nodes: List<A>): List<Edge<A>> =
        map { edge -> if (nodes.contains(edge.a) && nodes.contains(edge.b)) Option(edge) else None }
                .filter { it is Some }
                .mapNotNull { it.orNull() }


fun <A> Option<List<A>>.orEmptyList() =
        this.fold({ emptyList<A>() }, { it.id() })

fun Option<Boolean>.orFalse() =
        this.fold({ false }, { it.id() })
