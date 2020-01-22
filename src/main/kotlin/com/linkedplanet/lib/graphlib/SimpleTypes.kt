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
import arrow.core.Tuple2
import arrow.core.extensions.list.traverse.map

/**
 * Data class to represent an edge in a unweighted graph
 */
data class Edge<A>(val a: A, val b: A)

/**
 * Data class to represent a tree
 */
data class Tree<A>(val root: A, val subTrees: Option<List<Tree<A>>>)

// ---------------------------------------------
// Extension Functions
// ---------------------------------------------
val <A> Edge<A>.asTuple: Tuple2<A, A>
    get() = Tuple2(this.a, this.b)

val <A> Edge<A>.asPair: Pair<A,A>
    get() = Pair(this.a, this.b)

fun <A> Edge<A>.fromPair(p: Pair<A,A>): Edge<A> =
        Edge(p.first, p.second)

fun <A> Edge<A>.fromTuple(p: Tuple2<A, A>): Edge<A> =
        Edge(p.a, p.b)

val <A> Tree<A>.size: Int
    get() = when (this.subTrees) {
        is None -> 1
        is Some -> this.subTrees.map { it.map { a -> a.size } }
                .orEmptyList().max()!!.or(0) + 1
    }
