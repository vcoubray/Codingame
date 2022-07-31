package fr.vco.codingame.puzzles.mars.lander.engine

data class Action(val rotate: Int, val power: Int) {
    override fun toString() = "$rotate $power"
}


fun generateAction(): Action {
    return Action(
        (-15..15).random(),
        (-1..1).random()
    )
}

