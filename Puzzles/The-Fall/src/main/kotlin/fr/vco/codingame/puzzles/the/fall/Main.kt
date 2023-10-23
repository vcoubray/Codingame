package fr.vco.codingame.puzzles.the.fall

import kotlin.system.measureTimeMillis

fun main() {
    var maxTime = 0L
    val board = GameReader.readBoard()
    val gamePlayer = GamePlayer(board)

    while (true) {
        val indy = GameReader.readIndyPosition()
        val rocks = GameReader.readRocksPositions()

        measureTimeMillis {
            gamePlayer.play(indy, rocks)
        }.let {
            if (it > maxTime) maxTime = it
            System.err.println("Turn in ${it}ms")
        }
        System.err.println("Max turn in ${maxTime}ms")
    }
}
