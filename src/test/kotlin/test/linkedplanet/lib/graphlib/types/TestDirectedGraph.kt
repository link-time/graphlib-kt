package test.linkedplanet.lib.graphlib.types

import arrow.core.None
import arrow.core.Some
import arrow.core.extensions.option.foldable.size
import com.linkedplanet.lib.graphlib.graphtypes.DirectedGraph
import com.linkedplanet.lib.graphlib.Edge
import com.linkedplanet.lib.graphlib.size
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
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

    context("Test pathfinding") {
        val testSubject = DirectedGraph(listOf(
                Edge("A", "B"),
                Edge("A", "C"),
                Edge("C", "E"),
                Edge("E", "L"),
                Edge("L", "K"),
                Edge("K", "I"),
                Edge("I", "J"),
                Edge("F", "G"),
                Edge("G", "H"),
                Edge("D", "E")
        ))

        test("Simple Path") {
            testSubject.pathExists("A","B") shouldBe true
            testSubject.pathExists("A","F") shouldBe false
        }
        test("Long Path") {
            testSubject.getPath("A","J") shouldNotBe None
            testSubject.pathExists("A","J") shouldBe true
        }
    }

    context("Test Tree generation") {
        val testSubject = DirectedGraph(listOf(
                Edge("A", "B"),
                Edge("A", "C"),
                Edge("C", "E"),
                Edge("D", "E")
        ))

        test("Subtree count") {
            testSubject.getContainedTrees().size shouldBe 2
        }

        test("Tree size") {
            testSubject.getContainedTrees().first().map{ it.size } shouldBe Some(3)
        }

        test("Subtree top nodes") {
            testSubject.getContainedTrees().first().orNull()?.root shouldBe "A"
            testSubject.getContainedTrees()[1].orNull()?.root shouldBe "D"
        }

        test("Tree termination") {
            testSubject.getContainedTrees()[0].orNull()?.subTrees?.orNull()?.first()?.subTrees shouldNotBe None
            testSubject.getContainedTrees()[1].orNull()?.subTrees?.orNull()?.first()?.subTrees shouldBe None
        }

    }
})

