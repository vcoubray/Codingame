package fr.vco.codingame.puzzles.mars.lander.algorithm

import fr.vco.codingame.puzzles.mars.lander.engine.Action
import fr.vco.codingame.puzzles.mars.lander.engine.CapsuleState

class Chromosome(var actions: Array<Action>) {
    var score = -1.0
    val state = CapsuleState()
}