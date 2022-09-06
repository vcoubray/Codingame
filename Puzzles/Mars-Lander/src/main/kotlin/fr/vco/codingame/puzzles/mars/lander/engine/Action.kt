package fr.vco.codingame.puzzles.mars.lander.engine

import fr.vco.codingame.puzzles.mars.lander.POWER_RANGE
import fr.vco.codingame.puzzles.mars.lander.ROTATE_RANGE


data class Action(var rotate: Int, var power: Int) {
    fun randomize() {
        rotate = ROTATE_RANGE.random()
        power = POWER_RANGE.random()
    }

    override fun toString() = "$rotate $power"
}