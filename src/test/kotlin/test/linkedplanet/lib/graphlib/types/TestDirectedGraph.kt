package test.linkedplanet.lib.graphlib.types

import com.linkedplanet.lib.graphlib.graphtypes.DirectedGraph
import com.linkedplanet.lib.graphlib.Edge
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec


class TestDirectedGraph : FunSpec({

    context("Test cycling graph") {
        val testSubject = DirectedGraph(listOf(
                Edge("A", "B"),
                Edge("A", "C"),
                Edge("B", "D"),
                Edge("C", "E"),
                Edge("D", "B"), // Cycle is here
                Edge("D", "E"),
                Edge("E", "F"),
                Edge("F", "G"),
                Edge("G", "H"),
                Edge("G", "I"),
                Edge("G", "J"),
                Edge("I", "J"),
                Edge("I", "K"),
                Edge("J", "K"),
                Edge("K", "L"),
                Edge("K", "M")
        ))

        test("Test Node Count") {
            testSubject.getVerticesCount() shouldBe 13
        }

        test("Test Edge Count") {
            testSubject.getEdgeCount() shouldBe 16
        }

        test("Test cycle check") {
            testSubject.hasCycle() shouldBe true
        }
    }

    context("Test non-cycling graph") {
        val testSubject = DirectedGraph(listOf(
                Edge("A", "B"),
                Edge("A", "C"),
                Edge("B", "D"),
                Edge("C", "E"),
                Edge("D", "M"),
                Edge("D", "E"),
                Edge("E", "F"),
                Edge("F", "G"),
                Edge("G", "H"),
                Edge("G", "I"),
                Edge("G", "J"),
                Edge("I", "J"),
                Edge("I", "K"),
                Edge("J", "K"),
                Edge("K", "L"),
                Edge("K", "M")
        ))

        test("Test Node Count") {
            testSubject.getVerticesCount() shouldBe 13
        }

        test("Test Edge Count") {
            testSubject.getEdgeCount() shouldBe 16
        }

        test("Test cycle check") {
            testSubject.hasCycle() shouldBe false
        }
    }
})

