package fr.vco.codingame.contest.tictactoe

import java.util.*


fun main() {
    val input = Scanner(System.`in`)


    // val freeCells = List(3){MutableList(3){true}}

    // game loop
    while (true) {
        val opponentRow = input.nextInt()
        val opponentCol = input.nextInt()
        val validActionCount = input.nextInt()

        val freeCells = List(validActionCount){
            input.nextInt() to input.nextInt()
        }

        freeCells.randomOrNull()?.let{(x,y)->println("$x $y")}

    }
}