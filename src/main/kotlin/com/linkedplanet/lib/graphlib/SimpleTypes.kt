/*
 * MIT License
 *
 * Copyright (c) 2020 link-time GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
