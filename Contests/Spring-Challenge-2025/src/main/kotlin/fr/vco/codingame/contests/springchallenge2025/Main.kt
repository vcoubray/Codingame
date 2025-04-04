package fr.vco.codingame.contests.springchallenge2025

import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.pow
import kotlin.math.roundToInt
//
//val neighbours = listOf(
//    listOf(1, 3),
//    listOf(0, 2, 4),
//    listOf(1, 5),
//    listOf(0, 4, 6),
//    listOf(1, 3, 5, 7),
//    listOf(2, 4, 8),
//    listOf(3, 7),
//    listOf(6, 4, 8),
//    listOf(5, 7)
//)

val NEIGHBOURS_COMBINATIONS = listOf(
    listOf(listOf(1, 3)),
    listOf(listOf(2, 4), listOf(0, 4), listOf(0, 2), listOf(0, 2, 4)),
    listOf(listOf(1, 5)),
    listOf(listOf(4, 6), listOf(0, 6), listOf(0, 4), listOf(0, 4, 6)),
    listOf(
        listOf(5, 7),
        listOf(3, 7),
        listOf(3, 5),
        listOf(3, 5, 7),
        listOf(1, 7),
        listOf(1, 5),
        listOf(1, 5, 7),
        listOf(1, 3),
        listOf(1, 3, 7),
        listOf(1, 3, 5),
        listOf(1, 3, 5, 7)
    ),
    listOf(listOf(4, 8), listOf(2, 8), listOf(2, 4), listOf(2, 4, 8)),
    listOf(listOf(3, 7)),
    listOf(listOf(4, 8), listOf(6, 8), listOf(6, 4), listOf(6, 4, 8)),
    listOf(listOf(5, 7)),
)


fun main() {

    val input = Scanner(System.`in`)
    val depth = readln().toInt()
    val dies = List(3){readln().split(" ").map{it.toInt()}}.flatten()
//    val depth = 1
//    val dies = "555005555".map { it.digitToInt() }

    val state = State(dies, 0)

    val toVisit = ArrayDeque<State>().apply { add(state) }
    val res = mutableListOf<Int>()
    while (toVisit.isNotEmpty()) {
        val curr = toVisit.removeFirst()
        if (curr.isFinal()) {
            res.add(curr.getHash())
            continue
        }

        if (curr.turn == depth) {
            res.add(curr.getHash())
            continue
        }
        curr.getNextStates().forEach {
            toVisit.addLast(it)
        }
    }

    println(res.fold(0) { a, b -> (a + b) % (2.0.pow(30).roundToInt()) })

}

fun List<Int>.combinations(minSize: Int) {
    (0..2.0.pow(size).roundToInt()).map {
        val combination = it.toString(2).padStart(size, '0')
        this.filterIndexed { i, _ -> combination[i] == '1' }
    }.filter { it.size >= minSize }
        .joinToString(",", "listOf(", ")") { "listOf(${it.joinToString(",")})" }
        .let(::println)
}


data class State(val die: List<Int>, val turn: Int) {

    fun getNextStates(): Set<State> {
        val next = mutableSetOf<State>()
        die.forEachIndexed { i, value ->
            if (value == 0) {
                val combinations = NEIGHBOURS_COMBINATIONS[i]
                    .map { combination -> combination.sumOf { n -> die[n] } }
                    .mapIndexedNotNull { j, total -> if (total <= 6 && NEIGHBOURS_COMBINATIONS[i][j].count{n -> die[n] !=0} >= 2) j to total else null }


                if (combinations.isNotEmpty()) {
                    combinations.forEach { (j, total) ->
                        next.add(
                            State(
                                die.toMutableList().apply {
                                    this[i] = total
                                    NEIGHBOURS_COMBINATIONS[i][j].forEach { n -> this[n] = 0 }
                                },
                                turn + 1
                            )
                        )

                    }
                } else {
                    next.add(State(die.toMutableList().apply { this[i] = 1 }, turn + 1))
                }
            }
        }
        return next
    }

    fun isFinal() = die.none { it == 0 }

    fun getHash(): Int {
        var hash = 0
        die.forEach { hash = 10 * hash + it }
        return hash
    }


}