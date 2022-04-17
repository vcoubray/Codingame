package fr.vco.codingame.contest.tictactoe.game

data class Action(val row: Int, val col: Int, val player: Int) {
    override fun toString() = "$row $col"
}