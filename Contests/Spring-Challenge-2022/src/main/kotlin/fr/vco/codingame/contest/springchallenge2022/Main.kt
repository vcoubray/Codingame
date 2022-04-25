package fr.vco.codingame.contest.springchallenge2022

import java.util.*
import kotlin.math.hypot
import kotlin.system.measureTimeMillis

const val MAX_X = 17630
const val MAX_Y = 9000

const val MONSTER = 0
const val MY_HERO = 1
const val OPP_HERO = 2

const val TARGETING_BASE = 1
const val NO_TARGETING = 0

const val THREAD_NOBODY = 0
const val THREAD_ME = 1
const val THREAD_OPP = 2

const val BASE_VISION = 6000
const val HERO_VISION = 2200

const val HERO_MOVEMENT = 800
const val HERO_ATTACK_RANGE = 800
const val BASE_RANGE = 5000
const val RANGE_CONTROL = 2200
const val RANGE_SHIELD = 2200
const val RANGE_WIND = 1280
val DEFENDER_RANGE = hypot(MAX_X.toDouble(), MAX_Y.toDouble()) / 2

fun log(message: Any?) = System.err.println(message)

enum class Role {
    DEFENDER,
    ATTACKER
}


fun main() {
    val input = Scanner(System.`in`)
    val game = Game(input)

    // game loop
    while (true) {
        measureTimeMillis {
            game.update(input)
            game.play()
        }.let { log("played in ${it}ms") }
    }
}

