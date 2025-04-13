package fr.vco.codingame.contests.springchallenge2025

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import kotlin.test.Test

import kotlin.test.assertEquals

class MainTests {

    companion object {

        @JvmStatic
        fun tests() = listOf(
            Arguments.of(1, 20, "060222161", 322444322),
            Arguments.of(2, 20, "506450064", 951223336),
            Arguments.of(3, 1, "555005555", 36379286),
            Arguments.of(4, 1, "616101616", 264239762),
            Arguments.of(5, 8, "606000615", 76092874),
            Arguments.of(6, 24, "300362102", 661168294),
            Arguments.of(7, 36, "604202400", 350917228),
            Arguments.of(8, 32, "000054105", 999653138),
            Arguments.of(9, 40, "004024134", 521112022),
            Arguments.of(10, 40, "054030030", 667094338),
            Arguments.of(11, 20, "051000401", 738691369),
            Arguments.of(12, 20, "100352100", 808014757),
        )
    }

    @ParameterizedTest
    @MethodSource("tests")
    fun allTests(testCase: Int, depth: Int, board: String, expected: Int) {

        var initialBoard = 0
        repeat(BOARD_SIZE) {
            initialBoard = initialBoard.add(it, board[it].digitToInt())
        }

        val times = List(10) {
            measureTimeMillis {
                assertEquals(expected, resolve(initialBoard, depth+1))
            }
        }
        println("Resolved test \t$testCase in \t${times.mean()}ms. \tMax: ${times.max()}ms, \tMin: ${times.min()}ms")
    }


    @Test
    fun generateNeighboursCombination() {

        val neighbours = listOf(
            listOf(1, 3),
            listOf(0, 2, 4),
            listOf(1, 5),
            listOf(0, 4, 6),
            listOf(1, 3, 5, 7),
            listOf(2, 4, 8),
            listOf(3, 7),
            listOf(6, 4, 8),
            listOf(5, 7)
        )

        val combinations = neighbours.map{it.combinations(2)}
        println("arrayOf(")
        combinations.forEach {
            println(
                "${it.joinToString(",", "arrayOf(", ")") { "intArrayOf(${it.joinToString(",")})" }},"
            )
        }
        println(")")
    }
}

fun List<Long>.mean() = this.sum() / this.size

fun List<Int>.combinations(minSize: Int): List<List<Int>> {
    return (0..2.0.pow(size).roundToInt()).map {
        val combination = it.toString(2).padStart(size, '0')
        this.filterIndexed { i, _ -> combination[i] == '1' }
    }.filter { it.size >= minSize }
}




