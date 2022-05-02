package fr.vco.codingame.contest.springchallenge2022

sealed interface Action {

    fun manaCost() : Int

    object Wait : Action {
        override fun manaCost() = 0
        override fun toString() = "WAIT"
    }

    class Move(private val pos: Pos) : Action {
        override fun manaCost() = 0
        override fun toString() = "MOVE $pos"
    }

    class Wind( val dir: Pos) : Action {
        override fun manaCost() = SPELL_COST
        override fun toString() = "SPELL WIND $dir"
    }

    class Shield( val target: Entity) : Action {
        override fun manaCost() = SPELL_COST
        override fun toString() = "SPELL SHIELD ${target.id}"
    }

    class Control( val target: Entity,  val destination: Pos) : Action {
        override fun manaCost() = SPELL_COST
        override fun toString() = "SPELL CONTROL ${target.id} $destination"
    }
}