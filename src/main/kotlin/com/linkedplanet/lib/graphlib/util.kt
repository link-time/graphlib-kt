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
