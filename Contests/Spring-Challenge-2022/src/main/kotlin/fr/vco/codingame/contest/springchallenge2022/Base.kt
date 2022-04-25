package fr.vco.codingame.contest.springchallenge2022

import java.util.*

data class Base(val pos: Pos) {
    var health: Int = 0
    var mana: Int = 0

    fun update(input: Scanner) {
        health = input.nextInt()
        mana = input.nextInt()
    }
}

fun Pos.withRef(base: Base): Pos {
    return if (base.pos.x == 0) this
    else base.pos - this
}
