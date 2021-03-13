package fr.vco.codingame.contest.code4life

interface Action
class WaitAction : Action {
    override fun toString() = "WAIT"
}

class MoveAction : Action {
    override fun toString() = "Moving"
}

class GotoAction(val module: Module) : Action {
    override fun toString() = "GOTO $module"
}

class SearchAction(val rank: Int) : Action {
    override fun toString() = "CONNECT $rank"
}

class SampleAction(val sample: Sample) : Action {
    override fun toString() = "CONNECT ${sample.id}"
}

class MoleculeAction(val molecule: String) : Action {
    override fun toString() = "CONNECT $molecule"
}
