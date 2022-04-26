package fr.vco.codingame.contest.springchallenge2022

sealed interface Action {
    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Move(private val pos: Pos) : Action {
        override fun toString() = "MOVE $pos"
    }

    class Wind(private val pos: Pos) : Action {
        override fun toString() = "SPELL WIND $pos"
    }

    class Shield(private val target: Entity) : Action {
        override fun toString() = "SPELL SHIELD ${target.id}"
    }

    class Control(private val target: Entity, private val destination: Pos) : Action {
        override fun toString() = "SPELL CONTROL ${target.id} $destination"
    }
}