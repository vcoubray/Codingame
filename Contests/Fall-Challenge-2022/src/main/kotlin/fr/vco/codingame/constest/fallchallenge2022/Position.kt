package fr.vco.codingame.constest.fallchallenge2022

import kotlin.math.absoluteValue

data class Position(val x: Int, val y: Int) {
    operator fun plus(pos: Position) = Position(x + pos.x, y + pos.y)
    fun dist(pos: Position) = (x - pos.x).absoluteValue + (y - pos.y).absoluteValue
    override fun toString() = "$x $y"
}