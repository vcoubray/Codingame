package fr.vco.codingame.constest.fallchallenge2022

import java.util.*

fun log(message: Any?) = System.err.println(message)

fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt()
    val height = input.nextInt()

    val game = Game(height, width)

    while (true) {
        game.play(input)
    }
}

