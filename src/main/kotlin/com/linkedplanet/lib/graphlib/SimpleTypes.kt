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

/**
 * Data class to represent an edge in a unweighted graph
 */
typealias Edge<A> = Pair<A,A>
typealias Predicate<A> = (A)->Boolean

/**
 * Data class to represent a tree
 */
data class Tree<A>(val root: A, val subTrees: List<Tree<A>>?)

// ---------------------------------------------
// Extension Functions
// ---------------------------------------------
fun <A> Edge<A>.fromPair(p: Pair<A,A>): Edge<A> =
        Edge(p.first, p.second)

val <A> Tree<A>.size: Int
    get() = when (subTrees) {
        null -> 1
        else -> subTrees.foldRight(1) { a, acc -> acc + a.size }
    }
