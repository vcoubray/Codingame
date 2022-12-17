package fr.vco.codingame.constest.fallchallenge2022

sealed interface Action {

    class Move(val n: Int, val source: Position, val target: Position) : Action {
        override fun toString() = "MOVE $n $source $target"
    }

    class Spawn(val n: Int, val target: Position) : Action {
        override fun toString() = "SPAWN $n $target"
    }

    class Build(val target: Position) : Action {
        override fun toString() = "BUILD $target"
    }

    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Message(val message: String) : Action {
        override fun toString() = "MESSAGE $message"
    }
}