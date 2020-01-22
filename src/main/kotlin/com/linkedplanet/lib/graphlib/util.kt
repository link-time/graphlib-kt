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
