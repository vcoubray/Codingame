package fr.vco.codingame.contest.springchallenge2022

import java.util.*
import kotlin.system.measureTimeMillis

fun log(message: Any?) = System.err.println(message)

fun main() {
    val input = Scanner(System.`in`)
    val game = Game(input)
    val formations = Formations().apply{init(game.myBase, game.oppBase)}
    // game loop
    while (true) {
        measureTimeMillis {
            game.update(input)
            Player(game,formations).play()
        }.let { log("played in ${it}ms") }
    }
}
