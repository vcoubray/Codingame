package fr.vco.codingame.puzzles.roller.coaster

import java.util.*

fun main() {
    val input = Scanner(System.`in`)
    val size = input.nextInt()
    val maxTurns = input.nextInt()
    val queueSize = input.nextInt()
    val queue = List(queueSize) { input.nextInt() }

    val gains = MutableList(queueSize) { 0L }
    val alreadyPassed = mutableMapOf<Int, Int>()
    var gain = 0L
    var currentSize = 0
    var queueHead = 0
    var turn = 0

    while (turn < maxTurns) {
        val firstGroup = queueHead
        if (alreadyPassed.contains(firstGroup)) {
            val leftTurns = maxTurns - turn
            val cycleTurns = turn - alreadyPassed[firstGroup]!!
            val cycleTime = leftTurns / cycleTurns
            gain += gains[firstGroup] * cycleTime
            turn += cycleTime * cycleTurns
            if (turn >= maxTurns) break

            alreadyPassed.clear()
        }

        alreadyPassed[firstGroup] = turn
        var turnGain = 0
        do {
            currentSize += queue[queueHead]
            turnGain += queue[queueHead]
            queueHead = (queueHead + 1) % queueSize
        } while (size >= currentSize + queue[queueHead] && queueHead != firstGroup)

        alreadyPassed.forEach { (k, _) -> gains[k] = gains[k] + turnGain }
        gain += turnGain
        currentSize = 0
        turn++
    }

    println(gain)
}
