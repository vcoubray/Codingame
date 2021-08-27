package fr.vco.codingame.puzzles.`super`.calculateur

import java.util.*

data class Task(val start: Int, val end: Int)

fun main() {
    val input = Scanner(System.`in`)
    val N = input.nextInt()
    var lastEnd = 0

    val maxTasks = List(N) {
        val start = input.nextInt()
        val end = input.nextInt() + start - 1
        Task(start, end)
    }.sortedBy { it.end }.fold(0) { acc, it ->
        if (it.start > lastEnd) {
            lastEnd = it.end
            acc + 1
        } else acc
    }
    println(maxTasks)
}