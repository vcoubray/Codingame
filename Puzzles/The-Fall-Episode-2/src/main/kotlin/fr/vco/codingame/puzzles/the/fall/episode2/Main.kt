package fr.vco.codingame.puzzles.the.fall.episode2

import java.util.*

fun main() {
    val input = Scanner(System.`in`)
    val W = input.nextInt() // number of columns.
    val H = input.nextInt() // number of rows.
    if (input.hasNextLine()) {
        input.nextLine()
    }
    for (i in 0 until H) {
        val LINE = input.nextLine() // each line represents a line in the grid and contains W integers T. The absolute value of T specifies the type of the room. If T is negative, the room cannot be rotated.
    }
    val EX = input.nextInt() // the coordinate along the X axis of the exit.

    // game loop
    while (true) {
        val XI = input.nextInt()
        val YI = input.nextInt()
        val POSI = input.next()
        val R = input.nextInt() // the number of rocks currently in the grid.
        for (i in 0 until R) {
            val XR = input.nextInt()
            val YR = input.nextInt()
            val POSR = input.next()
        }

        // Write an action using println()
        // To debug: System.err.println("Debug messages...");


        // One line containing on of three commands: 'X Y LEFT', 'X Y RIGHT' or 'WAIT'
        println("WAIT")
    }
}